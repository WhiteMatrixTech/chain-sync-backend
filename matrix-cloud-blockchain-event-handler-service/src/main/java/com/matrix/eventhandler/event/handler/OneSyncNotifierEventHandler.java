package com.matrix.eventhandler.event.handler;

import com.matrix.eventhandler.model.BlockChainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO
 *
 * @author reimia
 */
// @Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OneSyncNotifierEventHandler implements BlockchainEventHandler {

  @Override
  public boolean isApplicable(final BlockChainEvent blockChainEvent) {
    return false;
  }

  @Override
  public void processBlockChainEvent(final BlockChainEvent blockChainEvent) {}
}
