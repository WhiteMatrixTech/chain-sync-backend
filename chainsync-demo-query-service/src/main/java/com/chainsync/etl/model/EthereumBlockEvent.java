package com.chainsync.etl.model;

import com.chainsync.dynamodb.annotation.DynamoAttribute;
import com.chainsync.dynamodb.annotation.DynamoKey;
import com.chainsync.dynamodb.annotation.DynamoTable;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author luyuanheng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
@DynamoTable
public class EthereumBlockEvent {

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
}
