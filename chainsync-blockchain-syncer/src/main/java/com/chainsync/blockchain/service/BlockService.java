package com.chainsync.blockchain.service;

import com.chainsync.blockchain.model.GetOffsetRequest;
import com.chainsync.blockchain.model.GetTransactionEventsRequest;
import com.chainsync.blockchain.model.GetTransactionEventsResponse;

/**
 * @author luyuanheng
 */
public interface BlockService {

  /**
   * get transaction events by transactionHash
   *
   * @param request
   * @return events list
   */
  GetTransactionEventsResponse getTransactionEvents(GetTransactionEventsRequest request);

  /**
   * get offset
   *
   * @param request
   * @return chainId offset
   */
  Long getOffset(GetOffsetRequest request);
}
