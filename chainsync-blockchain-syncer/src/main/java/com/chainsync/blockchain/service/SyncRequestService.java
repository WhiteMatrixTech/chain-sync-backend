package com.chainsync.blockchain.service;

import com.chainsync.blockchain.model.BlockRange;
import com.chainsync.blockchain.model.RetryRequest;
import com.chainsync.blockchain.model.SyncResponse;
import com.chainsync.blockchain.model.SyncStep;

/**
 * @author luyuanheng
 */
public interface SyncRequestService {

  /**
   * sync from latest task, step default 10
   *
   * @param syncStep sync block step
   * @return sync block result
   */
  SyncResponse runSyncTask(SyncStep syncStep);

  /**
   * sync the specified interval block
   *
   * @param blockRange {from: 10, to: 100}
   * @return sync block result
   */
  SyncResponse rangeSyncTask(BlockRange blockRange);

  /**
   * retry the failed request
   *
   * @param request
   * @return sync block result
   */
  SyncResponse retrySyncTask(RetryRequest request);
}
