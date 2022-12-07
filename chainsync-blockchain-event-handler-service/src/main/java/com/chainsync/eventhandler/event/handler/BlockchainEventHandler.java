package com.chainsync.eventhandler.event.handler;

import com.chainsync.eventhandler.model.BlockChainEvent;

/**
 * receive a parsed event and spread it
 *
 * @author reimia
 */
public interface BlockchainEventHandler {

  String getGroup();

  /** should the handler impl handle this event */
  boolean isApplicable(BlockChainEvent blockChainEvent);

  /** do handle event */
  void processBlockChainEvent(BlockChainEvent blockChainEvent);
}
