package com.matrix.etl.service;

import com.matrix.etl.model.response.QueryAppResponse;
import com.matrix.etl.model.response.QueryBlockResponse;
import com.matrix.etl.model.response.QueryEventsResponse;
import com.matrix.etl.model.response.QueryHandlerResponse;
import com.matrix.etl.model.response.QueryTaskResponse;
import com.matrix.etl.model.response.QueryTransactionResponse;

/**
 * @author richard
 */
public interface BlockchainService {

  QueryTaskResponse queryTask();

  QueryHandlerResponse queryHandler();

  QueryAppResponse queryApp();

  QueryBlockResponse queryBlock(String chainType, Long blockNumber);

  QueryTransactionResponse queryTransaction(String chainType, String transactionHash);

  QueryEventsResponse queryEvents(String chainType);
}
