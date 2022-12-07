package com.chainsync.blockchain.service;

import com.chainsync.blockchain.model.RetryRequest;
import com.chainsync.blockchain.model.BlockRange;
import com.chainsync.blockchain.model.SyncResult;
import com.chainsync.blockchain.model.SyncStep;

public interface SyncerService {

  /**
   * sync from latest task, step default 10
   *
   * @param syncStep sync block step
   * @return sync block result
   */
  SyncResult runSyncTask(SyncStep syncStep);

  /**
   * sync the specified interval block
   *
   * @param blockRange {from: 10, to: 100}
   * @return sync block result
   */
  SyncResult rangeSyncTask(BlockRange blockRange);

  /**
   * retry the failed request
   *
   * @param request
   * @return sync block result
   */
  SyncResult retrySyncTask(RetryRequest request);
}
