package com.matrix.blockchain.processor;

import com.matrix.blockchain.model.BlockEvent;
import com.matrix.blockchain.model.BlockList;
import com.matrix.blockchain.model.BlockRange;
import com.matrix.blockchain.model.GetTransactionEventsRequest;
import com.matrix.blockchain.model.GetTransactionEventsResponse;
import com.matrix.blockchain.model.SyncResponse;
import com.matrix.blockchain.model.SyncResponse.Builder;
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
