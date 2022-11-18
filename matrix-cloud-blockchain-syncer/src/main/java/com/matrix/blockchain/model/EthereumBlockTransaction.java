package com.matrix.blockchain.model;

import com.matrix.dynamodb.annotation.DynamoAttribute;
import com.matrix.dynamodb.annotation.DynamoGSIKey;
import com.matrix.dynamodb.annotation.DynamoKey;
import com.matrix.dynamodb.annotation.DynamoTable;
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
@DynamoTable(globalSecondaryIndices = {EthereumBlockTransaction.INDEX_TRANSACTION_HASH})
public class EthereumBlockTransaction {

  public static final String INDEX_TRANSACTION_HASH = "index_transactionHash";

  @DynamoKey
  @DynamoAttribute(attributeName = "blockNumber", attributeType = "N")
  private Long blockNumber;

  @DynamoKey(dynamoKeyType = DynamoKey.RANGE)
  @DynamoGSIKey(dynamoGSINames = INDEX_TRANSACTION_HASH)
  @DynamoAttribute(attributeName = "transactionHash")
  private String transactionHash;

  @DynamoAttribute(attributeName = "nonce")
  private String nonce;
  @DynamoAttribute(attributeName = "transactionIndex")
  private String transactionIndex;
  @DynamoAttribute(attributeName = "fromAddress")
  private String fromAddress;
  @DynamoAttribute(attributeName = "toAddress")
  private String toAddress;
  @DynamoAttribute(attributeName = "value")
  private String value;
  @DynamoAttribute(attributeName = "gas")
  private String gas;
  @DynamoAttribute(attributeName = "gasPrice")
  private String gasPrice;
  @DynamoAttribute(attributeName = "input")
  private String input;
  @DynamoAttribute(attributeName = "blockHash")
  private String blockHash;
}
