package com.chainsync.eventhandler.model;

import com.chainsync.common.model.Address;
import com.chainsync.common.model.ChainId;
import com.chainsync.common.model.ChainName;
import com.chainsync.common.model.ChainType;
import java.util.List;
import java.util.Map;
import lombok.Value;
import org.web3j.abi.datatypes.Type;

/**
 * this event handles all chain with evm such as ethereum mainnet and polygon, chainType can be get
 * from contract Address
 *
 * @author reimia
 */
@Value
public class EvmEvent implements BlockChainEvent {

  public EvmEvent(
      final BlockchainEventLogDTO blockchainEventLogDTO, final AbiEnhancedEvent abiEnhancedEvent) {
    this.eventName = abiEnhancedEvent.getName();
    this.eventHash = abiEnhancedEvent.getEventHash();
    this.contract =
        Address.fromAddressAndChainId(
            blockchainEventLogDTO.getAddress(),
            ChainId.builder()
                .chainName(ChainName.valueOf(blockchainEventLogDTO.getChainName()))
                .chainType(ChainType.valueOf(blockchainEventLogDTO.getChainType()))
                .build());
    this.blockNumber = blockchainEventLogDTO.getBlockNumber();
    this.blockTimeStamp = blockchainEventLogDTO.getBlockTimestamp();

    this.rawPayload = blockchainEventLogDTO.getData();
    this.payload =
        abiEnhancedEvent.decodeToMap(
            blockchainEventLogDTO.getTopics(), blockchainEventLogDTO.getData());
    this.transactionHash = blockchainEventLogDTO.getTransactionHash();

    // TODO
    this.eventTags = List.of();
  }

  String eventName;
  String eventHash;
  Address contract;
  String transactionHash;
  Long blockNumber;
  Long blockTimeStamp;
  String rawPayload;
  Map<String, Type<?>> payload;
  List<String> eventTags;
}
