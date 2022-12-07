package com.chainsync.blockchain.processor;

import com.chainsync.blockchain.handler.FlowSyncHandler;
import com.chainsync.blockchain.model.BlockEvent;
import com.chainsync.blockchain.model.FailedBlock;
import com.chainsync.blockchain.model.GetTransactionEventsRequest;
import com.chainsync.blockchain.model.SyncResponse;
import com.chainsync.blockchain.model.SyncResponse.Builder;
import com.chainsync.blockchain.model.SyncStatus;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.chainsync.blockchain.model.BlockList;
import com.chainsync.blockchain.model.BlockRange;
import com.chainsync.blockchain.model.GetTransactionEventsResponse;
import com.chainsync.blockchain.model.TransactionEvent;
import com.chainsync.common.model.ChainType;
import com.chainsync.marketplace.model.BlockchainTransaction;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
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

  @Resource
  FlowSyncHandler flowSyncHandler;

  @Value("${s3.event.flow.bucket}")
  private String flowEventBucket;

  @Override
  public List<BlockEvent> process(BlockRange blockRange) {
    return List.of();
  }

  @Override
  public boolean isApplicable(final String chainType) {
    return ChainType.flow.name().equalsIgnoreCase(chainType);
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
