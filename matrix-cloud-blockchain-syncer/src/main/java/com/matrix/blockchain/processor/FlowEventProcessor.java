package com.matrix.blockchain.processor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.matrix.blockchain.constants.Constants;
import com.matrix.blockchain.dao.BlockTipDao;
import com.matrix.blockchain.handler.FlowSyncHandler;
import com.matrix.blockchain.model.BlockEvent;
import com.matrix.blockchain.model.BlockList;
import com.matrix.blockchain.model.BlockRange;
import com.matrix.blockchain.model.BlockTip;
import com.matrix.blockchain.model.BlockchainType;
import com.matrix.blockchain.model.FailedBlock;
import com.matrix.blockchain.model.FlowBlockEvent;
import com.matrix.blockchain.model.GetTransactionEventsRequest;
import com.matrix.blockchain.model.GetTransactionEventsResponse;
import com.matrix.blockchain.model.NotifyStatus;
import com.matrix.blockchain.model.SyncResponse;
import com.matrix.blockchain.model.SyncResponse.Builder;
import com.matrix.blockchain.model.SyncStatus;
import com.matrix.blockchain.model.TransactionEvent;
import com.matrix.blockchain.retriever.FlowEventRetriever;
import com.matrix.common.model.ChainType;
import com.matrix.dynamodb.dao.BaseQueryDao;
import com.matrix.marketplace.blockchain.model.BlockchainTransaction;
import com.matrix.metric.util.MetricUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import proto.v1.BlockEventsResponseEvent;
import schedule.proto.Event;
import schedule.proto.FailedHeight;
import schedule.proto.ScheduleJobResp;
import schedule.proto.Transaction;

/**
 * @author luyuanheng
 */
@Log4j2
@Component
public class FlowEventProcessor extends CommonEventProcessor {

  @Resource FlowEventRetriever eventRetriever;
  @Resource BlockTipDao blockTipDao;
  @Resource MeterRegistry meterRegistry;
  @Resource private Map<String, BaseQueryDao<FlowBlockEvent>> flowBlockEventDaoMap;
  @Resource FlowSyncHandler flowSyncHandler;

  @Value("${s3.event.flow.bucket}")
  private String flowEventBucket;

  @Override
  public List<BlockEvent> process(BlockRange blockRange) {
    try {
      BaseQueryDao flowBlockEventDao =
          flowBlockEventDaoMap.get(BlockchainType.getBlockEventDao(blockRange.getChainId()));

      final long blockHeight = eventRetriever.getBlockHeight(blockRange.getChainId());
      if (blockRange.getFrom() > blockHeight) {
        return Lists.newArrayList();
      }
      if (blockRange.getTo() > blockHeight) {
        blockRange = blockRange.toBuilder().setTo(blockHeight).build();
      }

      // step 0. query block tip
      final BlockTip tip = blockTipDao.getItem(blockRange.getChainId());
      if (tip.getBlockNumber() >= blockRange.getTo()) {
        return super.queryEvents(flowBlockEventDao, blockRange);
      }

      // step 1. get event logs from chain
      final List<BlockEventsResponseEvent> logs = eventRetriever.retrieveEvents(blockRange);
      final Map<Long, Block> blockMap = new HashMap<>();

      // step 2. persistent events into ddb
      final List<BlockEvent> events =
          filterAndConvertEvents(blockMap, logs, super.getEventMap(flowBlockEventDao, blockRange));
      super.persistentEvents(flowBlockEventDao, blockRange, events);

      // step 3. notify through mq
      // super.notifyEvents(blockRange, events);

      // step 4. update event status to SENT
      // updateEventStatus(flowBlockEventDao, events);

      final List<BlockchainTransaction> transactions =
          filterAndConvertTransactions(logs, super.getTransactionMap(blockRange));

      // step 5. persistent & notify transactions
      super.processTransactions(blockRange, transactions);

      // step 6. update tip
      blockTipDao.updateTip(blockRange);

      return events;
    } catch (final Exception e) {
      log.error(
          "sync chainType: {}, from: {}, to: {}, error: {}",
          blockRange.getChainType(),
          blockRange.getFrom(),
          blockRange.getTo(),
          e);
      MetricUtil.addGauge(
          meterRegistry,
          "blockchain_sync_error",
          1,
          Tag.of("ChainType", blockRange.getChainType()));

      super.persistentError(blockRange, e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isApplicable(final String chainType) {
    return ChainType.flow.name().equalsIgnoreCase(chainType);
  }

  private List<BlockEvent> filterAndConvertEvents(
      final Map<Long, Block> blockMap,
      final List<BlockEventsResponseEvent> events,
      final Map<String, BlockEvent> eventMap) {
    final List<BlockEvent> blockEvents = new ArrayList<>();
    for (final BlockEventsResponseEvent event : events) {
      final FlowBlockEvent blockEvent = FlowBlockEvent.convertFromEventLog(event);

      blockEvent.resolveEventKey(
          blockEvent.getBlockNumber(), blockEvent.getTransactionIndex(), blockEvent.getLogIndex());
      if (isExceeded(blockEvent.getData())) {
        blockEvent.setPersistenceData(true);
        blockEvent.setData(
            uploadS3(
                blockEvent.getData(),
                Constants.EVENT_FOLDER,
                getEventDataS3Key(
                    ChainType.flow.name(),
                    blockEvent.getTransactionHash(),
                    blockEvent.getLogIndex())));
      }
      final String key = blockEvent.getKey();
      if (eventMap.get(key) == null
          || NotifyStatus.NOT_SENT
              .name()
              .equalsIgnoreCase(((FlowBlockEvent) eventMap.get(key)).getStatus())) {
        blockEvent.setStatus(NotifyStatus.NOT_SENT.name());
        final long blockTimestamp =
            blockMap.get(Long.valueOf(event.getBlockId())) == null
                ? System.currentTimeMillis() / 1000
                : blockMap.get(Long.valueOf(event.getBlockId())).getTimestamp().longValue();
        blockEvent.setBlockTimestamp(blockTimestamp);
        blockEvents.add(blockEvent);
      }
    }

    return blockEvents;
  }

  private List<BlockchainTransaction> filterAndConvertTransactions(
      final List<BlockEventsResponseEvent> events,
      final Map<String, BlockchainTransaction> transactionMap) {
    final Map<String, BlockchainTransaction> notSendTransactionMap = new LinkedHashMap<>();
    // remove duplicate event
    final Map<String, Set<String>> transactionEventsMap = new HashMap<>();
    for (final BlockEventsResponseEvent event : events) {
      final String transactionHash = event.getTransactionId();
      if (transactionMap.get(transactionHash) == null
          || NotifyStatus.NOT_SENT
              .name()
              .equalsIgnoreCase(transactionMap.get(event.getTransactionId()).getStatus())) {
        if (notSendTransactionMap.get(transactionHash) == null) {
          final BlockchainTransaction transaction =
              BlockchainTransaction.builder()
                  .chainType(ChainType.flow.name())
                  .blockNumber(event.getHeight())
                  .transactionIndex(event.getTransactionIndex())
                  .transactionHash(transactionHash)
                  .status(NotifyStatus.NOT_SENT.name())
                  .build();
          transaction.resolveIdAndKey();
          notSendTransactionMap.put(transactionHash, transaction);
          transactionEventsMap.put(transactionHash, new HashSet<>());
        }
        transactionEventsMap.get(transactionHash).add(event.getType());
      }
    }

    // set events from set to list
    for (final Entry<String, BlockchainTransaction> entry : notSendTransactionMap.entrySet()) {
      entry.getValue().setEvents(new ArrayList<>(transactionEventsMap.get(entry.getKey())));
    }
    return new ArrayList<>(notSendTransactionMap.values());
  }

  @Override
  public SyncResponse syncRequest(BlockList blockList) {
    Builder builder =
        SyncResponse.newBuilder().setBlockList(blockList).setStatus(SyncStatus.SUCCESS.name());

    List<Long> blockNumbers = Lists.newArrayList(blockList.getBlockNumbersList());
    List<Long> successBlocks =
        blockList.getForceFromChain() ? Lists.newArrayList() : querySuccessBlocks(blockList);
    blockNumbers.removeAll(successBlocks);
    // blocks have been synced
    if (blockNumbers.isEmpty()) {
      builder.addAllSuccessBlocks(successBlocks);
      return builder.build();
    }

    blockList = blockList.toBuilder().clearBlockNumbers().addAllBlockNumbers(blockNumbers).build();
    ScheduleJobResp resp = flowSyncHandler.handleSyncRequest(blockList);
    Map<Long, String> failedBlockMap = Maps.newHashMap();
    if (resp.getFailedCount() > 0) {
      for (FailedHeight failedHeight : resp.getFailedList()) {
        failedBlockMap.put(failedHeight.getHeight(), failedHeight.getErrorsList().toString());
        log.info(
            "chainId: {}, sync block failed, block: {}",
            blockList.getChainId(),
            failedHeight.getHeight());
      }
    }

    // set each block status
    List<FailedBlock> failedBlocks = Lists.newArrayList();
    for (Long b : blockNumbers) {
      if (failedBlockMap.containsKey(b)) {
        failedBlocks.add(
            FailedBlock.newBuilder().setHeight(b).setErrorMessage(failedBlockMap.get(b)).build());
      } else {
        successBlocks.add(b);
      }
    }
    builder.addAllSuccessBlocks(successBlocks);
    builder.addAllFailedBlocks(failedBlocks);

    // save block status
    super.saveSuccessBlocks(blockList, successBlocks);
    super.saveFailedBlocks(blockList, failedBlocks);
    log.info(
        "save block status success, success: {}, failed: {}",
        successBlocks,
        failedBlocks.stream().map(FailedBlock::getHeight).collect(Collectors.toList()));

    // set request status
    if (!CollectionUtils.isEmpty(failedBlocks)) {
      if (CollectionUtils.isEmpty(successBlocks)) {
        builder.setStatus(SyncStatus.FAILED.name());
      } else {
        builder.setStatus(SyncStatus.PART_SUCCESS.name());
      }
    }

    return builder.build();
  }

  @Override
  public BlockRange resetBlockRange(BlockRange blockRange) {
    long blockHeight = flowSyncHandler.getBlockHeight(blockRange.getChainId());
    if (blockRange.getTo() > blockHeight - blockRange.getBlockBuff()) {
      blockRange = blockRange.toBuilder().setTo(blockHeight - blockRange.getBlockBuff()).build();
    }

    return blockRange;
  }

  @Override
  public void processBlocks(SyncResponse syncResponse) {
    BlockList blockList = syncResponse.getBlockList();
    // step 1. get blocks from s3
    List<schedule.proto.Block> blocks =
        super.getBlocksFromS3(
            blockList.getChainType(),
            blockList.getChainName(),
            flowEventBucket,
            syncResponse.getSuccessBlocksList());

    // step 2. extract transactions & cache
    List<BlockchainTransaction> transactions = extractTransactions(blockList, blocks);

    // step 3. send transactions
    if (blockList.getHistory()) {
      super.notifyTransactionsHistory(blockList, transactions);
    } else {
      super.notifyTransactions(blockList, transactions);
    }
  }

  private List<BlockchainTransaction> extractTransactions(
      BlockList blockList, List<schedule.proto.Block> blocks) {
    List<BlockchainTransaction> transactions = Lists.newArrayList();

    if (!CollectionUtils.isEmpty(blocks)) {
      for (schedule.proto.Block block : blocks) {
        if (block.getTransactionsCount() > 0) {
          for (Transaction transaction : block.getTransactionsList()) {
            List<String> eventTypes = Lists.newArrayList();
            if (transaction.getEventsCount() > 0) {
              for (Event event : transaction.getEventsList()) {
                // to be send to kafka
                eventTypes.add(event.getType());
              }
            }
            transactions.add(
                BlockchainTransaction.builder()
                    .blockNumber(block.getHeight())
                    .chainType(blockList.getChainType())
                    .transactionIndex(transaction.getIndex())
                    .transactionHash(transaction.getId())
                    .events(eventTypes)
                    .build());
          }
        }
      }
    }

    return transactions;
  }

  private Map<String, List<TransactionEvent>> getTransactions(List<schedule.proto.Block> blocks) {
    Map<String, List<TransactionEvent>> transactionEventsMap = Maps.newHashMap();
    if (!CollectionUtils.isEmpty(blocks)) {
      for (schedule.proto.Block block : blocks) {
        if (block.getTransactionsCount() > 0) {
          for (Transaction transaction : block.getTransactionsList()) {
            List<TransactionEvent> eventList = Lists.newArrayList();
            if (transaction.getEventsCount() > 0) {
              for (Event event : transaction.getEventsList()) {
                eventList.add(convertToTransactionEvent(block, transaction, event));
              }
              transactionEventsMap.put(transaction.getId(), eventList);
            }
          }
        }
      }
    }

    return transactionEventsMap;
  }

  @NotNull
  private TransactionEvent convertToTransactionEvent(
      schedule.proto.Block block, Transaction transaction, Event event) {
    TransactionEvent.Builder builder =
        TransactionEvent.newBuilder()
            .setBlockNumber(block.getHeight())
            .setTransactionIndex(transaction.getIndex())
            .setLogIndex(event.getEventIndex())
            .setTransactionHash(transaction.getId())
            .setType(event.getType())
            .setTimestamp(block.getTimestamp());
    if (event.getValuesCount() > 0) {
      Map<String, String> values = Maps.newHashMap();
      for (schedule.proto.Value value : event.getValuesList()) {
        values.put(value.getName(), value.getValue());
      }
      builder.setData(gson.toJson(values));
    } else {
      builder.setData(gson.toJson(new Object()));
    }
    return builder.build();
  }

  @Override
  public GetTransactionEventsResponse getTransactionEvents(GetTransactionEventsRequest request) {
    GetTransactionEventsResponse.Builder builder = GetTransactionEventsResponse.newBuilder();

    List<schedule.proto.Block> blocks =
        getBlocksFromS3(
            request.getChainType(),
            request.getChainName(),
            flowEventBucket,
            Lists.newArrayList(request.getBlockNumber()));

    Map<String, List<TransactionEvent>> transactionEventsMap = getTransactions(blocks);
    List<TransactionEvent> flowEvents = transactionEventsMap.get(request.getTransactionHash());

    if (!CollectionUtils.isEmpty(flowEvents)) {
      builder.addAllEvents(flowEvents);
    }

    return builder.build();
  }
}
