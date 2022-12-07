package com.matrix.blockchain.service.impl;

import com.google.common.base.Preconditions;
import com.matrix.blockchain.constants.Constants;
import com.matrix.blockchain.dao.BlockTipDao;
import com.matrix.blockchain.dao.SyncErrorDao;
import com.matrix.blockchain.model.BlockEvent;
import com.matrix.blockchain.model.BlockRange;
import com.matrix.blockchain.model.BlockTip;
import com.matrix.blockchain.model.RetryRequest;
import com.matrix.blockchain.model.SyncError;
import com.matrix.blockchain.model.SyncResult;
import com.matrix.blockchain.model.SyncResult.Builder;
import com.matrix.blockchain.model.SyncStatus;
import com.matrix.blockchain.model.SyncStep;
import com.matrix.blockchain.processor.EventProcessor;
import com.matrix.blockchain.service.SyncerService;
import com.matrix.common.model.ChainId;
import com.matrix.common.model.ChainName;
import com.matrix.common.model.ChainType;
import com.matrix.dynamodb.model.CursorQueryResult;
import com.matrix.dynamodb.model.CursorQuerySpec;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author luyuanheng
 */
@Log4j2
@Service
public class SyncerServiceImpl implements SyncerService {

  @Resource List<EventProcessor> eventProcessors;

  @Resource BlockTipDao blockTipDao;

  @Resource SyncErrorDao syncErrorDao;

  /**
   * sync from latest task, step default 10
   *
   * @param syncStep sync block step
   * @return sync block result
   */
  @Override
  public SyncResult runSyncTask(SyncStep syncStep) {
    Preconditions.checkArgument(syncStep.getChainType() != null, "chain type can not be null");
    Preconditions.checkArgument(
        Math.abs(syncStep.getStep()) <= Constants.BLOCK_MAX_RANGE,
        "sync step must less than " + Constants.BLOCK_MAX_RANGE);

    ChainId chainId =
        ChainId.builder()
            .chainType(ChainType.valueOf(syncStep.getChainType()))
            .chainName(ChainName.valueOf(syncStep.getChainName()))
            .build();
    BlockTip tip = blockTipDao.getItem(chainId.toString());
    Preconditions.checkArgument(
        tip != null && tip.getBlockNumber() != null,
        String.format("chain type: %s need to initialize in db", syncStep.getChainType()));

    BlockRange blockRange =
        BlockRange.newBuilder()
            .setChainType(syncStep.getChainType())
            .setChainName(syncStep.getChainName())
            .setChainId(chainId.toString())
            .setFrom(tip.getBlockNumber() + 1)
            .setTo(tip.getBlockNumber() + syncStep.getStep())
            .setBlockBuff(syncStep.getBlockBuff())
            .build();
    return rangeSyncTask(blockRange);
  }

  /**
   * sync the specified interval block
   *
   * @param blockRange {from: 10, to: 100}
   * @return sync block result
   */
  @Override
  public SyncResult rangeSyncTask(BlockRange blockRange) {
    Preconditions.checkArgument(
        blockRange.getTo() >= blockRange.getFrom(), "block range to must bigger than from");
    Preconditions.checkArgument(
        blockRange.getTo() - blockRange.getFrom() <= Constants.BLOCK_MAX_RANGE,
        String.format("block range less than %s", Constants.BLOCK_MAX_RANGE));

    Builder builder =
        SyncResult.newBuilder().setBlockRange(blockRange).setChainType(blockRange.getChainType());

    try {
      Long start = System.currentTimeMillis();
      for (EventProcessor eventProcessor : eventProcessors) {
        if (eventProcessor.isApplicable(blockRange.getChainType())) {
          List<BlockEvent> events = eventProcessor.process(blockRange);
          builder.setStatus(SyncStatus.SUCCESS.name());

          log.info(
              "sync task success, block chainId: {}, from: {} to: {}, event size: {}, cost: {} mills",
              blockRange.getChainId(),
              blockRange.getFrom(),
              blockRange.getTo(),
              events.size(),
              System.currentTimeMillis() - start);
          break;
        }
      }
    } catch (Exception e) {
      log.error(
          "sync task failed, block chainId: {}, from: {} to: {}, error: {}",
          blockRange.getChainId(),
          blockRange.getFrom(),
          blockRange.getTo(),
          e);
      builder.setStatus(SyncStatus.FAILED.name());
      builder.setErrorMessage(e.getMessage());
    }

    return builder.build();
  }

  /**
   * retry the failed request
   *
   * @param request
   * @return sync block result
   */
  @Override
  public SyncResult retrySyncTask(RetryRequest request) {
    ChainId chainId =
        ChainId.builder()
            .chainType(ChainType.valueOf(request.getChainType()))
            .chainName(ChainName.valueOf(request.getChainName()))
            .build();
    // query failed blocks
    CursorQueryResult<SyncError> result =
        syncErrorDao.queryByCursor(
            chainId.toString(),
            CursorQuerySpec.builder().limit(request.getCount()).ascending(true).build());
    SyncResult syncResult = SyncResult.newBuilder().build();
    if (result != null && !CollectionUtils.isEmpty(result.getItems())) {
      for (SyncError syncError : result.getItems()) {
        syncResult =
            rangeSyncTask(
                BlockRange.newBuilder()
                    .setChainType(request.getChainType())
                    .setChainName(request.getChainName())
                    .setChainId(chainId.toString())
                    .setFrom(syncError.getBlockNumber())
                    .setTo(Long.valueOf(syncError.getErrorDetail().split(":")[0]))
                    .setForceFromChain(true)
                    .build());
        if (SyncStatus.SUCCESS.name().equals(syncResult.getStatus())) {
          syncErrorDao.deleteItem(chainId.toString(), syncError.getBlockNumber());
        }
      }
    }

    return syncResult;
  }
}
