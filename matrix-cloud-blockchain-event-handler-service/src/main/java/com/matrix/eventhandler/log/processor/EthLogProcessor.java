package com.matrix.eventhandler.log.processor;

import com.matrix.common.model.ChainId;
import com.matrix.common.model.ChainName;
import com.matrix.common.model.ChainType;
import com.matrix.common.model.EthereumAddress;
import com.matrix.eventhandler.abi.AbiEnhancedEventManager;
import com.matrix.eventhandler.model.AbiEnhancedEvent;
import com.matrix.eventhandler.model.BlockChainEvent;
import com.matrix.eventhandler.model.BlockchainEventLogDTO;
import com.matrix.eventhandler.model.EvmEvent;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author reimia
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EthLogProcessor implements BlockchainLogProcessor {

  private final AbiEnhancedEventManager abiEnhancedEventManager;

  @Override
  public ChainType getBlockchainType() {
    return ChainType.ethereum;
  }

  @Override
  public boolean canProcess(final BlockchainEventLogDTO blockChainLog) {
    if (blockChainLog.getTopics().isEmpty()) {
      return false;
    }
    final String rawAddress = blockChainLog.getAddress();
    final EthereumAddress ethereumAddress =
        new EthereumAddress(rawAddress, getChainId(blockChainLog));
    final List<AbiEnhancedEvent> abiEvent = abiEnhancedEventManager.getAbiEvent(ethereumAddress);
    return abiEvent.stream()
        .anyMatch(
            abiEnhancedEvent -> abiEnhancedEvent.isLogMatched(blockChainLog.getTopics().get(0)));
  }

  @Override
  public List<BlockChainEvent> processBlockchainLog(final BlockchainEventLogDTO blockChainLog) {
    final String rawAddress = blockChainLog.getAddress();
    final EthereumAddress ethereumAddress =
        new EthereumAddress(rawAddress, getChainId(blockChainLog));
    final List<AbiEnhancedEvent> abiEvent = abiEnhancedEventManager.getAbiEvent(ethereumAddress);
    return abiEvent.stream()
        .filter(event -> event.isLogMatched(blockChainLog.getTopics().get(0)))
        .map(abiEnhancedEvent -> new EvmEvent(blockChainLog, abiEnhancedEvent))
        .collect(Collectors.toList());
  }

  private ChainId getChainId(final BlockchainEventLogDTO blockChainLog) {
    return ChainId.builder()
        .chainName(ChainName.valueOf(blockChainLog.getChainName()))
        .chainType(ChainType.valueOf(blockChainLog.getChainType()))
        .build();
  }
}
