package com.chainsync.eventhandler.log.processor;

import com.chainsync.eventhandler.model.AbiEnhancedEvent;
import com.chainsync.eventhandler.model.BlockChainEvent;
import com.chainsync.common.model.ChainId;
import com.chainsync.common.model.ChainName;
import com.chainsync.common.model.ChainType;
import com.chainsync.common.model.EthereumAddress;
import com.chainsync.eventhandler.abi.AbiEnhancedEventManager;
import com.chainsync.eventhandler.model.BlockchainEventLogDTO;
import com.chainsync.eventhandler.model.EvmEvent;
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
            abiEnhancedEvent -> {
              if (abiEnhancedEvent.getName().equals("Transfer")
                  && blockChainLog.getTopics().size() != 4) {
                return false;
              }
              return abiEnhancedEvent.isLogMatched(blockChainLog.getTopics().get(0));
            });
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
