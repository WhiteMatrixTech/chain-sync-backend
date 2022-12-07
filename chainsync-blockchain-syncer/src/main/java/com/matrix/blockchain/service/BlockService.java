package com.matrix.blockchain.service;

import com.matrix.blockchain.model.GetOffsetRequest;
import com.matrix.blockchain.model.GetTransactionEventsRequest;
import com.matrix.blockchain.model.GetTransactionEventsResponse;

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
