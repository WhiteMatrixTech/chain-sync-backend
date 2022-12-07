package com.matrix.eventhandler.log;

import com.matrix.common.model.ChainType;
import com.matrix.eventhandler.event.BlockchainEventHandlerManager;
import com.matrix.eventhandler.log.processor.BlockchainLogProcessor;
import com.matrix.eventhandler.model.BlockChainEvent;
import com.matrix.eventhandler.model.BlockchainEventLogDTO;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
public class BlockchainLogProcessorManager {

  private final BlockchainEventHandlerManager blockchainEventHandlerManager;
  private final List<BlockchainLogProcessor> blockchainLogProcessors;
  private final Map<ChainType, BlockchainLogProcessor> map = new EnumMap<>(ChainType.class);

  public void handleBlockchainLog(
      final ChainType chainType, final BlockchainEventLogDTO blockChainLog) {
    if (!map.containsKey(chainType)) {
      throw new IllegalStateException("unsupported chainType!");
    }

    final boolean canProcess = map.get(chainType).canProcess(blockChainLog);
    if (canProcess) {
      log.info(
          "[BlockchainLogProcessorManager] blockEventLog contract address: [{}], blockNumber: [{}], transactionHash: [{}], transactionIndex: [{}], logIndex: [{}] can process",
          blockChainLog.getAddress(),
          blockChainLog.getBlockNumber(),
          blockChainLog.getTransactionHash(),
          blockChainLog.getTransactionIndex(),
          blockChainLog.getLogIndex());
      // handle the event we know contract abi and parsed raw log to event with event name
      final List<BlockChainEvent> blockChainEvents =
          map.get(chainType).processBlockchainLog(blockChainLog);
      blockChainEvents.forEach(
          blockChainEvent -> {
            log.info(
                "[BlockchainLogProcessorManager] retriever blockChainEvent success with contract address: [{}], eventName: [{}]",
                blockChainEvent.getContract(),
                blockChainEvent.getEventName());
            blockchainEventHandlerManager.handleProcessedBlockchainEvent(blockChainEvent);
          });
    } else {
      log.info(
          "[BlockchainLogProcessorManager] blockEventLog contract address: [{}], blockNumber: [{}], transactionHash: [{}], index: [{}], logIndex: [{}]  can not process",
          blockChainLog.getAddress(),
          blockChainLog.getBlockNumber(),
          blockChainLog.getTransactionHash(),
          blockChainLog.getTransactionIndex(),
          blockChainLog.getLogIndex());
    }
  }

  @PostConstruct
  void setup() {
    blockchainLogProcessors.forEach(
        blockchainLogProcessor ->
            map.put(blockchainLogProcessor.getBlockchainType(), blockchainLogProcessor));
    log.info(
        "[BlockchainLogProcessorManager] registered BlockchainLogProcessor size:{}, detail:{}",
        map.size(),
        map);
  }
}
