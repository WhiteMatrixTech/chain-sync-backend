package com.chainsync.blockchain.processor;

import com.chainsync.blockchain.model.BlockEvent;
import com.chainsync.blockchain.model.SyncResponse;
import com.chainsync.blockchain.model.BlockList;
import com.chainsync.blockchain.model.BlockRange;
import com.chainsync.blockchain.model.GetTransactionEventsRequest;
import com.chainsync.blockchain.model.GetTransactionEventsResponse;
import java.util.List;

/**
 * @author luyuanheng
 */
public interface EventProcessor {

  List<BlockEvent> process(BlockRange blockRange);

  SyncResponse syncRequest(BlockList blockList);

  void processBlocks(SyncResponse syncResponse);

  GetTransactionEventsResponse getTransactionEvents(GetTransactionEventsRequest request);

  boolean isApplicable(String chainType);

  BlockRange resetBlockRange(BlockRange blockRange);
}
