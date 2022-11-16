package com.matrix.eventhandler.log.processor;

import com.matrix.common.model.ChainType;
import com.matrix.eventhandler.model.BlockChainEvent;
import com.matrix.eventhandler.model.BlockchainEventLogDTO;
import java.util.List;

/**
 * LogProcessor receive a raw log from kafka and process it to BlockChainEvent
 *
 * @author reimia
 */
public interface BlockchainLogProcessor {

  ChainType getBlockchainType();

  /**
   * verity if the log is produced by our managed contract, if we have matched contract address abi,
   * we can parse the data in EthLogAvro
   */
  boolean canProcess(BlockchainEventLogDTO blockChainLog);

  /** convert log to event, parse eventName and tag event
   * @return*/
  List<BlockChainEvent> processBlockchainLog(BlockchainEventLogDTO blockChainLog);
}
