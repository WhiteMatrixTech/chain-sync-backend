package com.matrix.blockchain.model;

import static com.matrix.blockchain.constants.Constants.CONNECTOR;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.matrix.common.util.AddressUtil;
import com.matrix.dynamodb.annotation.DynamoAttribute;
import com.matrix.dynamodb.annotation.DynamoGSIKey;
import com.matrix.dynamodb.annotation.DynamoKey;
import com.matrix.dynamodb.annotation.DynamoTable;
import com.matrix.eventhandler.model.BlockchainEventLogDTO;
import com.matrix.marketplace.blockchain.util.PaddingUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import proto.v1.BlockEventsResponseEvent;
import proto.v1.BlockEventsResponseValue;

/**
 * @author luyuanheng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
@DynamoTable(globalSecondaryIndices = {FlowBlockEvent.INDEX_TX_HASH})
public class FlowBlockEvent extends BlockEvent {

  public static final String INDEX_TX_HASH = "txHashIndex";

  private static final Gson GSON = new Gson();

  @DynamoKey
  @DynamoAttribute(attributeName = "id", attributeType = "N")
  private Long id;

  @DynamoKey(dynamoKeyType = DynamoKey.RANGE)
  @DynamoAttribute(attributeName = "eventKey")
  private String eventKey;

  @DynamoAttribute(attributeName = "blockNumber", attributeType = "N")
  private Long blockNumber;

  @DynamoGSIKey(
      dynamoGSINames = {INDEX_TX_HASH},
      dynamoKeyType = DynamoGSIKey.RANGE)
  @DynamoAttribute(attributeName = "logIndex", attributeType = "N")
  private Long logIndex;

  @DynamoAttribute(attributeName = "transactionIndex", attributeType = "N")
  private Long transactionIndex;

  @DynamoGSIKey(dynamoGSINames = {INDEX_TX_HASH})
  @DynamoAttribute(attributeName = "transactionHash")
  private String transactionHash;

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

  @DynamoAttribute(attributeName = "status")
  private String status;

  @DynamoAttribute(attributeName = "updatedAt")
  private Instant updatedAt;

  public static FlowBlockEvent convertFromEventLog(final BlockEventsResponseEvent event) {
    FlowBlockEventBuilder builder =
        FlowBlockEvent.builder()
            .id(event.getHeight() / BlockEvent.BATCH_SIZE)
            .blockNumber(event.getHeight())
            .logIndex(event.getIndex())
            .transactionIndex(event.getTransactionIndex())
            .transactionHash(event.getTransactionId())
            .address(AddressUtil.normalizedFlowAddress(event.getType()))
            .type(event.getType())
            .blockTimestamp(event.getTimestamp().getSeconds())
            .status(NotifyStatus.NOT_SENT.name())
            .updatedAt(Instant.now());
    if (event.getValuesCount() > 0) {
      Map<String, String> values = Maps.newHashMap();
      for (BlockEventsResponseValue blockEventsResponseValue : event.getValuesList()) {
        values.put(blockEventsResponseValue.getName(), blockEventsResponseValue.getValue());
      }
      builder.data(GSON.toJson(values));
    } else {
      builder.data(GSON.toJson(new Object()));
    }
    return builder.build();
  }

  @Override
  public BlockchainEventLogDTO convertToAvro(BlockRange blockRange) {
    return BlockchainEventLogDTO.newBuilder()
        .setChainType(blockRange.getChainType())
        .setChainName(blockRange.getChainName())
        .setBlockNumber(this.getBlockNumber())
        .setLogIndex(this.getLogIndex())
        .setTransactionIndex(this.getTransactionIndex())
        .setTransactionHash(this.getTransactionHash())
        .setData(this.getData())
        .setPersistenceData(this.getPersistenceData())
        .setType(this.getType())
        .setBlockTimestamp(this.getBlockTimestamp())
        // flow has no below attr
        .setRemoved(false)
        .setBlockHash("")
        .setAddress(this.getAddress())
        .setTopics(new ArrayList<>())
        .build();
  }

  @Override
  public String getKey() {
    return this.getBlockNumber()
        + CONNECTOR
        + this.getTransactionIndex()
        + CONNECTOR
        + this.getLogIndex();
  }

  @Override
  public String getKafkaKey() {
    return this.getAddress();
  }

  /**
   * key = {0} * n + blockNumber + {0} * m + transactionIndex + {0} * p + logIndex
   *
   * @param blockNumber
   * @param transactionIndex
   * @param logIndex
   * @return eventKey
   */
  public void resolveEventKey(
      final Long blockNumber, final Long transactionIndex, final Long logIndex) {
    final String txAndLogIndex =
        PaddingUtil.paddingAndConnect(
            transactionIndex, TX_INDEX_LEN, logIndex, LOG_INDEX_LEN, PaddingUtil.ZERO);
    this.setEventKey(
        PaddingUtil.paddingAndConnect(
            blockNumber,
            BLOCK_NUMBER_LEN,
            txAndLogIndex,
            TX_INDEX_LEN + LOG_INDEX_LEN,
            PaddingUtil.ZERO));
  }
}
