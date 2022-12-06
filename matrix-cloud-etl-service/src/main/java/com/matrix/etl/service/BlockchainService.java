package com.matrix.etl.service;

import com.matrix.etl.model.response.QueryBlockResponse;
import com.matrix.etl.model.response.QueryEventsResponse;
import com.matrix.etl.model.response.QueryHandlerResponse;
import com.matrix.etl.model.response.QueryTaskLogResponse;
import com.matrix.etl.model.response.QueryTaskResponse;
import com.matrix.etl.model.response.QueryTransactionResponse;

/**
 * @author richard
 */
public interface BlockchainService {

  QueryTaskResponse queryTask();

  QueryTaskLogResponse queryTaskName(String taskName);

  QueryHandlerResponse queryHandler();


  QueryBlockResponse queryBlock(String chainType, Long blockNumber);

  QueryTransactionResponse queryTransaction(String chainType, Long blockNumber);

  QueryEventsResponse queryEvents(String chainType);
}
