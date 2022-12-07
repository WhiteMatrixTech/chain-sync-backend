package com.chainsync.blockchain.model;

import com.chainsync.blockchain.constants.Constants;
import com.chainsync.dynamodb.annotation.DynamoAttribute;
import com.chainsync.dynamodb.annotation.DynamoKey;
import com.chainsync.dynamodb.annotation.DynamoTable;
import com.chainsync.eventhandler.model.BlockchainEventLogDTO;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.web3j.protocol.core.methods.response.EthLog.LogObject;

/**
 * @author luyuanheng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
@DynamoTable
public class EthereumBlockEvent extends BlockEvent {

  @DynamoKey
  @DynamoAttribute(attributeName = "id", attributeType = "N")
  private Long id;

  @DynamoKey(dynamoKeyType = DynamoKey.RANGE)
  @DynamoAttribute(attributeName = "eventKey")
  private String eventKey;

  @DynamoAttribute(attributeName = "blockNumber", attributeType = "N")
  private Long blockNumber;

  @DynamoAttribute(attributeName = "removed", attributeType = "B")
  private boolean removed;

  @DynamoAttribute(attributeName = "logIndex", attributeType = "N")
  private Long logIndex;

  @DynamoAttribute(attributeName = "transactionIndex", attributeType = "N")
  private Long transactionIndex;

  @DynamoAttribute(attributeName = "transactionHash")
  private String transactionHash;

  @DynamoAttribute(attributeName = "blockHash")
  private String blockHash;

  @DynamoAttribute(attributeName = "address")
  private String address;

  @DynamoAttribute(attributeName = "data")
  private String data;

  // if data size has exceeded the maximum allowed size (400kb)
  // data store at s3, data field store s3 url
  @Builder.Default
  @DynamoAttribute(attributeName = "persistenceData")
  private Boolean persistenceData = false;

  @DynamoAttribute(attributeName = "type")
  private String type;

  @DynamoAttribute(attributeName = "blockTimestamp")
  private Long blockTimestamp;

  @DynamoAttribute(attributeName = "topics")
  private List<String> topics;

  @DynamoAttribute(attributeName = "status")
  private String status;

  @DynamoAttribute(attributeName = "updatedAt")
  private Instant updatedAt;

  public static EthereumBlockEvent convertFromEventLog(LogObject logObject) {
    return EthereumBlockEvent.builder()
        .id(logObject.getBlockNumber().longValue() / EthereumBlockEvent.BATCH_SIZE)
        .blockNumber(logObject.getBlockNumber().longValue())
        .removed(logObject.isRemoved())
        .logIndex(logObject.getLogIndex().longValue())
        .transactionIndex(logObject.getTransactionIndex().longValue())
        .transactionHash(logObject.getTransactionHash())
        .blockHash(logObject.getBlockHash())
        .address(logObject.getAddress())
        .data(logObject.getData())
        .type(logObject.getType())
        .topics(logObject.getTopics())
        .status(NotifyStatus.NOT_SENT.name())
        .updatedAt(Instant.now())
        .build();
  }

  @Override
  public BlockchainEventLogDTO convertToAvro(BlockRange blockRange) {
    return BlockchainEventLogDTO.newBuilder()
        .setChainType(blockRange.getChainType())
        .setChainName(blockRange.getChainName())
        .setBlockNumber(this.getBlockNumber())
        .setRemoved(this.isRemoved())
        .setLogIndex(this.getLogIndex())
        .setTransactionIndex(this.getTransactionIndex())
        .setTransactionHash(this.getTransactionHash())
        .setBlockHash(this.getBlockHash())
        .setAddress(this.getAddress())
        .setData(this.getData())
        .setPersistenceData(this.getPersistenceData())
        .setType(this.getType())
        .setBlockTimestamp(this.getBlockTimestamp())
        .setTopics(this.getTopics())
        .build();
  }

  @Override
  public String getKey() {
    return this.getBlockNumber() + Constants.CONNECTOR + this.getLogIndex();
  }

  @Override
  public String getKafkaKey() {
    return this.getAddress();
  }
}
