package com.matrix.blockchain.service.impl;

import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition;
import com.google.common.base.Preconditions;
import com.matrix.blockchain.constants.Constants;
import com.matrix.blockchain.dao.BlockFailedDao;
import com.matrix.blockchain.dao.BlockOffsetDao;
import com.matrix.blockchain.model.BlockFailed;
import com.matrix.blockchain.model.BlockList;
import com.matrix.blockchain.model.BlockOffset;
import com.matrix.blockchain.model.BlockRange;
import com.matrix.blockchain.model.RetryRequest;
import com.matrix.blockchain.model.SyncResponse;
import com.matrix.blockchain.model.SyncStatus;
import com.matrix.blockchain.model.SyncStep;
import com.matrix.blockchain.processor.EventProcessor;
import com.matrix.blockchain.service.SyncRequestService;
import com.matrix.common.model.ChainId;
import com.matrix.common.model.ChainName;
import com.matrix.common.model.ChainType;
import com.matrix.dynamodb.model.CursorQueryResult;
import com.matrix.dynamodb.model.CursorQuerySpec;
import com.matrix.metric.util.MetricUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author luyuanheng
 */
@Log4j2
@Service
public class SyncRequestServiceImpl implements SyncRequestService {

  @Resource List<EventProcessor> eventProcessors;

  @Resource BlockOffsetDao blockOffsetDao;

  @Resource BlockFailedDao blockFailedDao;

  @Resource MeterRegistry meterRegistry;

  private static LongAdder ERROR_COUNT = new LongAdder();
  /**
   * sync from latest task, step default 10
   *
   * @param syncStep sync block step
   * @return sync block result
   */
  @Override
  public SyncResponse runSyncTask(SyncStep syncStep) {
    Preconditions.checkArgument(syncStep.getChainType() != null, "chain type can not be null");
    Preconditions.checkArgument(
        Math.abs(syncStep.getStep()) <= Constants.BLOCK_MAX_RANGE,
        "sync step must less than " + Constants.BLOCK_MAX_RANGE);

    ChainId chainId =
        ChainId.builder()
            .chainType(ChainType.valueOf(syncStep.getChainType()))
            .chainName(ChainName.valueOf(syncStep.getChainName()))
            .build();
    BlockOffset offset = blockOffsetDao.getItem(chainId.toString(), syncStep.getStart());
    if (offset == null) {
      offset =
          blockOffsetDao.putItem(
              BlockOffset.builder()
                  .chainId(chainId.toString())
                  .start(syncStep.getStart())
                  .end(syncStep.getEnd())
                  .offset(syncStep.getStart())
                  .build());
    } else if (offset.getEnd() != null
        && offset.getEnd() > 0
        && (syncStep.getStep() > 0 && offset.getOffset() >= offset.getEnd()
            || syncStep.getStep() < 0 && offset.getOffset() <= offset.getEnd())) {
      log.info("chainId: {}, start: {} is finished", chainId, syncStep.getStart());
      return SyncResponse.newBuilder().setStatus(SyncStatus.FAILED.name()).build();
    }

    return rangeSyncTask(
        BlockRange.newBuilder()
            .setChainType(syncStep.getChainType())
            .setChainName(syncStep.getChainName())
            .setChainId(chainId.toString())
            .setStart(offset.getStart())
            .setEnd(offset.getEnd())
            .setFrom(syncStep.getStep() > 0 ? offset.getOffset() + 1 : offset.getOffset() - 1)
            .setTo(offset.getOffset() + syncStep.getStep())
            .setForceFromChain(syncStep.getForceFromChain())
            .setBlockBuff(syncStep.getBlockBuff())
            .setHistory(syncStep.getHistory())
            .build());
  }

  /**
   * sync the specified interval block
   *
   * @param blockRange {from: 10, to: 100}
   * @return sync block result
   */
  @Override
  public SyncResponse rangeSyncTask(BlockRange blockRange) {
    Preconditions.checkArgument(
        Math.abs(blockRange.getTo() - blockRange.getFrom()) <= Constants.BLOCK_MAX_RANGE,
        String.format("block range less than %s", Constants.BLOCK_MAX_RANGE));
    SyncResponse syncResponse = SyncResponse.newBuilder().build();
    for (EventProcessor eventProcessor : eventProcessors) {
      if (eventProcessor.isApplicable(blockRange.getChainType())) {
        try {
          if (!blockRange.getHistory()) {
            blockRange = eventProcessor.resetBlockRange(blockRange);
          }
          // step 1. sync request
          syncResponse =
              eventProcessor.syncRequest(
                  BlockList.newBuilder()
                      .setChainId(blockRange.getChainId())
                      .setChainName(blockRange.getChainName())
                      .setChainType(blockRange.getChainType())
                      .addAllBlockNumbers(getBlockNumbers(blockRange))
                      .setForceFromChain(blockRange.getForceFromChain())
                      .setHistory(blockRange.getHistory())
                      .build());
          log.info(
              "sync request success, block chainId: {}, from: {} to: {}, success: {}, failed: {}",
              blockRange.getChainId(),
              blockRange.getFrom(),
              blockRange.getTo(),
              syncResponse.getSuccessBlocksList(),
              syncResponse.getFailedBlocksList());

          // step 2. get block info from storage & handle block info
          eventProcessor.processBlocks(syncResponse);
          log.info(
              "process blocks success, block chainId: {}, from: {} to: {}",
              blockRange.getChainId(),
              blockRange.getFrom(),
              blockRange.getTo());

          // step 3. update offset
          blockOffsetDao.updateOffset(blockRange);
          log.info(
              "update offset success, block chainId: {}, from: {} to: {}",
              blockRange.getChainId(),
              blockRange.getFrom(),
              blockRange.getTo());
          break;
        } catch (Exception e) {
          ERROR_COUNT.increment();
          log.error(
              "sync chainId: {}, from: {}, to: {}, error: {}",
              blockRange.getChainId(),
              blockRange.getFrom(),
              blockRange.getTo(),
              e);
          MetricUtil.addGauge(
              meterRegistry,
              "blockchain_sync_error",
              ERROR_COUNT.longValue(),
              Tag.of("chainId", blockRange.getChainId()));
        }
      }
    }

    return syncResponse;
  }

  /**
   * retry the failed request
   *
   * @param request
   * @return sync block result
   */
  @Override
  public SyncResponse retrySyncTask(RetryRequest request) {
    ChainId chainId =
        ChainId.builder()
            .chainType(ChainType.valueOf(request.getChainType()))
            .chainName(ChainName.valueOf(request.getChainName()))
            .build();
    // query failed blocks
    CursorQueryResult<BlockFailed> result = getBlockFailedCursorQueryResult(request, chainId);

    SyncResponse syncResponse = SyncResponse.newBuilder().build();
    if (result != null && !CollectionUtils.isEmpty(result.getItems())) {
      List<Long> blockNumbers =
          result.getItems().stream().map(BlockFailed::getBlockNumber).collect(Collectors.toList());

      for (EventProcessor eventProcessor : eventProcessors) {
        if (eventProcessor.isApplicable(request.getChainType())) {
          // step 1. sync request
          syncResponse =
              eventProcessor.syncRequest(
                  BlockList.newBuilder()
                      .setChainId(chainId.toString())
                      .setChainName(request.getChainName())
                      .setChainType(request.getChainType())
                      .addAllBlockNumbers(blockNumbers)
                      .setForceFromChain(request.getForceFromChain())
                      .build());
          log.info(
              "sync request success, block chainId: {}, blockNumbers: {}, success: {}, failed: {}",
              chainId.toString(),
              blockNumbers,
              syncResponse.getSuccessBlocksList(),
              syncResponse.getFailedBlocksList());

          // step 2. get block info from storage & handle block info
          eventProcessor.processBlocks(syncResponse);
          log.info(
              "process blocks success, block chainId: {}, blockNumbers: {}",
              chainId.toString(),
              blockNumbers);

          // step 3. remove success blocks from failed table
          for (Long blockNumber : syncResponse.getSuccessBlocksList()) {
            blockFailedDao.deleteItem(chainId.toString(), blockNumber);
          }
          break;
        }
      }

      MetricUtil.addGauge(
          meterRegistry,
          "blockchain_sync_failed_count",
          blockFailedDao.queryCountByPartitionKey(chainId.toString()),
          new Tag[] {Tag.of("chainId", chainId.toString())});
    }

    return syncResponse;
  }

  private CursorQueryResult<BlockFailed> getBlockFailedCursorQueryResult(
      RetryRequest request, ChainId chainId) {
    CursorQueryResult<BlockFailed> result;
    long start = request.getStart();
    long end = request.getEnd();
    if (start > 0 || end > 0) {
      RangeKeyCondition rangeKeyCondition = new RangeKeyCondition(BlockFailed.ATTR_BLOCK_NUMBER);
      if (start > 0) {
        rangeKeyCondition.gt(start);
      }
      if (end > 0) {
        rangeKeyCondition.lt(end);
      }
      result =
          blockFailedDao.queryByCursorWithRangeCondition(
              chainId.toString(),
              CursorQuerySpec.builder().limit(request.getCount()).ascending(true).build(),
              rangeKeyCondition);
    } else {
      result =
          blockFailedDao.queryByCursor(
              chainId.toString(),
              CursorQuerySpec.builder().limit(request.getCount()).ascending(true).build());
    }
    return result;
  }

  private List<Long> getBlockNumbers(BlockRange blockRange) {
    List<Long> blockList = Lists.newArrayList();
    if (blockRange.getFrom() <= blockRange.getTo()) {
      for (long i = blockRange.getFrom(); i <= blockRange.getTo(); i++) {
        blockList.add(i);
      }
    } else {
      for (long i = blockRange.getFrom(); i >= blockRange.getTo(); i--) {
        blockList.add(i);
      }
    }

    return blockList;
  }
}
