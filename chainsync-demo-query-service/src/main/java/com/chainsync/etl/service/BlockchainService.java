package com.chainsync.etl.service;

import com.chainsync.etl.model.response.QueryBlockResponse;
import com.chainsync.etl.model.response.QueryEventsResponse;
import com.chainsync.etl.model.response.QueryHandlerResponse;
import com.chainsync.etl.model.response.QueryTaskLogResponse;
import com.chainsync.etl.model.response.QueryTaskResponse;
import com.chainsync.etl.model.response.QueryTransactionResponse;

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
