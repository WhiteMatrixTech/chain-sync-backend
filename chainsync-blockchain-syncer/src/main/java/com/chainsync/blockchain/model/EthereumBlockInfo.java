package com.chainsync.blockchain.model;

import com.chainsync.dynamodb.annotation.DynamoAttribute;
import com.chainsync.dynamodb.annotation.DynamoKey;
import com.chainsync.dynamodb.annotation.DynamoTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author richard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
@DynamoTable
public class EthereumBlockInfo {

  @DynamoKey
  @DynamoAttribute(attributeName = "blockNumber", attributeType = "N")
  private Long blockNumber;

  @DynamoAttribute(attributeName = "timestamp")
  private String timestamp;

  @DynamoAttribute(attributeName = "hash")
  private String hash;

  @DynamoAttribute(attributeName = "parentHash")
  private String parentHash;

  @DynamoAttribute(attributeName = "nonce")
  private String nonce;

  @DynamoAttribute(attributeName = "sha3Uncles")
  private String sha3Uncles;

  @DynamoAttribute(attributeName = "logsBloom")
  private String logsBloom;

  @DynamoAttribute(attributeName = "transactionsRoot")
  private String transactionsRoot;

  @DynamoAttribute(attributeName = "stateRoot")
  private String stateRoot;

  @DynamoAttribute(attributeName = "receiptsRoot")
  private String receiptsRoot;

  @DynamoAttribute(attributeName = "miner")
  private String miner;

  @DynamoAttribute(attributeName = "difficulty")
  private String difficulty;

  @DynamoAttribute(attributeName = "totalDifficulty")
  private String totalDifficulty;

  @DynamoAttribute(attributeName = "size")
  private String size;

  @DynamoAttribute(attributeName = "extraData")
  private String extraData;

  @DynamoAttribute(attributeName = "gasLimit")
  private String gasLimit;

  @DynamoAttribute(attributeName = "gasUsed")
  private String gasUsed;

  @DynamoAttribute(attributeName = "transactionCount")
  private Integer transactionCount;

  @DynamoAttribute(attributeName = "baseFeePerGas")
  private String baseFeePerGas;
}
