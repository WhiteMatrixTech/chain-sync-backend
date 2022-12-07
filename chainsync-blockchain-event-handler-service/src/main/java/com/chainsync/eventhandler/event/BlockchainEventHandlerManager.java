package com.chainsync.eventhandler.event;

import com.chainsync.eventhandler.event.handler.BlockchainEventHandler;
import com.chainsync.eventhandler.model.BlockChainEvent;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author reimia
 */
@Log4j2
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BlockchainEventHandlerManager {

  private final List<BlockchainEventHandler> blockchainEventHandlers;

  public void handleProcessedBlockchainEvent(final BlockChainEvent blockChainEvent) {
    for (final BlockchainEventHandler blockchainEventHandler : blockchainEventHandlers) {
      try {
        if (blockchainEventHandler.isApplicable(blockChainEvent)) {
          log.info(
              "[BlockchainEventHandlerManager] [{}] is going to handle event with contract address: [{}], eventName: [{}]",
              blockchainEventHandler.getClass().getName(),
              blockChainEvent.getContract(),
              blockChainEvent.getEventName());
          blockchainEventHandler.processBlockChainEvent(blockChainEvent);
          log.info(
              "[BlockchainEventHandlerManager] [{}] successfully handle event with contract address: [{}], eventName: [{}]",
              blockchainEventHandler.getClass().getName(),
              blockChainEvent.getContract(),
              blockChainEvent.getEventName());
          // TODO save process result
        }
      } catch (final Exception e) {
        // TODO handle exception
        log.error("[BlockchainEventHandlerManager] handleProcessedBlockchainEvent error", e);
      }
    }
  }

  public List<BlockchainEventHandler> getBlockchainEventHandlers() {
    return blockchainEventHandlers;
  }

  @PostConstruct
  void setup() {
    log.info(
        "[BlockchainEventHandlerManager] registered BlockchainEventHandler size:{}, detail: {}",
        blockchainEventHandlers.size(),
        blockchainEventHandlers);
  }
}
