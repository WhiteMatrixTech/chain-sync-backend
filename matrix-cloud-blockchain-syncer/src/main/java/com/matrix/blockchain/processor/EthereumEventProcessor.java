package com.matrix.blockchain.processor;

import com.matrix.blockchain.constants.Constants;
import com.matrix.blockchain.model.BlockEvent;
import com.matrix.blockchain.model.BlockRange;
import com.matrix.blockchain.model.BlockTip;
import com.matrix.blockchain.model.BlockTransaction;
import com.matrix.blockchain.model.BlockchainType;
import com.matrix.blockchain.model.EthereumBlockEvent;
import com.matrix.blockchain.model.NotifyStatus;
import com.matrix.blockchain.model.Web3jContainer;
import com.matrix.blockchain.retriever.EvmEventRetriever;
import com.matrix.common.model.ChainType;
import com.matrix.dynamodb.dao.BaseQueryDao;
import com.matrix.metric.util.MetricUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthLog.LogObject;

/**
 * @author luyuanheng
 */
@Log4j2
@Component
public class EthereumEventProcessor extends CommonEventProcessor {

  @Resource EvmEventRetriever eventRetriever;

  @Resource MeterRegistry meterRegistry;

  @Resource private Map<String, Web3jContainer> web3jMap;

  @Resource private Map<String, BaseQueryDao<EthereumBlockEvent>> ethBlockEventDaoMap;

  @Resource private Map<String, BaseQueryDao<BlockTransaction>> ethBlockTransactionDaoMap;

  @Override
  public List<BlockEvent> process(BlockRange blockRange) {
    try {
      final Web3jContainer web3jContainer =
          web3jMap.get(BlockchainType.getWeb3j(blockRange.getChainId()));
      final BaseQueryDao ethBlockEventDao =
          ethBlockEventDaoMap.get(BlockchainType.getBlockEventDao(blockRange.getChainId()));
      final BaseQueryDao ethBlockTransactionDao =
          ethBlockTransactionDaoMap.get(
              BlockchainType.getBlockchainTransactionDao(blockRange.getChainId()));

      final long blockHeight = eventRetriever.getBlockHeight(web3jContainer);
      if (blockRange.getFrom() > blockHeight - blockRange.getBlockBuff()) {
        return Collections.emptyList();
      }
      if (blockRange.getTo() > blockHeight - blockRange.getBlockBuff()) {
        blockRange = blockRange.toBuilder().setTo(blockHeight - blockRange.getBlockBuff()).build();
      }

      // step 0. query block tip
      final BlockTip tip = blockTipDao.getItem(blockRange.getChainId());
      if (!blockRange.getForceFromChain() && tip.getBlockNumber() >= blockRange.getTo()) {
        return super.queryEvents(ethBlockEventDao, blockRange);
      }

      // step 1. get event logs from chain
      final List<LogObject> logs = eventRetriever.retrieveEvents(web3jContainer, blockRange);
      final Map<Long, Block> blockMap = eventRetriever.retrieveBlocks(web3jContainer, blockRange);

      // step 2. persistent events into ddb
      final List<BlockEvent> events =
          filterAndConvertEvents(
              blockMap, logs, super.getEventMap(ethBlockEventDao, blockRange), blockRange);
      super.persistentEvents(ethBlockEventDao, blockRange, events);

      // persistent transaction info into ddb
      super.persistentTransactions(ethBlockTransactionDao, blockRange, blockMap);

      // currently only notify ethereum event to downstream
      if (blockRange.getChainType().equalsIgnoreCase(ChainType.ethereum.name())) {
        // step 3. notify through mq
        super.notifyEvents(blockRange, events);

        // step 4. update event status to SEND
        updateEventStatus(ethBlockEventDao, events);
      }

      // step 5. update tip
      blockTipDao.updateTip(blockRange);
      return events;
    } catch (final Exception e) {
      log.error(
          "eth sync chainType: {}, from: {}, to: {}, error: {}",
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
    return ChainType.ethereum.name().equalsIgnoreCase(chainType)
        || ChainType.polygon.name().equalsIgnoreCase(chainType)
        || ChainType.bsc.name().equalsIgnoreCase(chainType);
  }

  private List<BlockEvent> filterAndConvertEvents(
      final Map<Long, Block> blockMap,
      final List<LogObject> events,
      final Map<String, BlockEvent> eventMap,
      final BlockRange blockRange) {
    final List<BlockEvent> blockEvents = new ArrayList<>();
    for (final LogObject event : events) {
      final EthereumBlockEvent blockEvent = EthereumBlockEvent.convertFromEventLog(event);
      blockEvent.setEventKey(
          blockEvent.resolveEventKey(blockEvent.getBlockNumber(), blockEvent.getLogIndex()));
      if (isExceeded(event.getData())) {
        blockEvent.setPersistenceData(true);
        blockEvent.setData(
            uploadS3(
                event.getData(),
                Constants.EVENT_FOLDER,
                getEventDataS3Key(
                    blockRange.getChainType(),
                    blockEvent.getTransactionHash(),
                    blockEvent.getLogIndex())));
      }
      final String key = blockEvent.getKey();
      if (eventMap.get(key) == null
          || NotifyStatus.NOT_SENT
              .name()
              .equalsIgnoreCase(((EthereumBlockEvent) eventMap.get(key)).getStatus())) {
        blockEvent.setStatus(NotifyStatus.NOT_SENT.name());
        blockEvent.setBlockTimestamp(
            blockMap.get(event.getBlockNumber().longValue()).getTimestamp().longValue());
        blockEvents.add(blockEvent);
      }
    }

    return blockEvents;
  }

  private void updateEventStatus(final BaseQueryDao blockEventDao, final List<BlockEvent> events) {
    final Instant now = Instant.now();
    for (final BlockEvent event : events) {
      final EthereumBlockEvent ethereumBlockEvent = ((EthereumBlockEvent) event);
      ethereumBlockEvent.setStatus(NotifyStatus.SENT.name());
      ethereumBlockEvent.setUpdatedAt(now);
    }
    blockEventDao.parallelPutItem(events);
  }
}
