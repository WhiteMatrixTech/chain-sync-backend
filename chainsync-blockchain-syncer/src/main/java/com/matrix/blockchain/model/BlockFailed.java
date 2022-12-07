package com.matrix.blockchain.model;

import com.matrix.dynamodb.annotation.DynamoAttribute;
import com.matrix.dynamodb.annotation.DynamoKey;
import com.matrix.dynamodb.annotation.DynamoTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author luyuanheng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoTable
public class BlockFailed {

  public static final String ATTR_CHAIN_ID = "chainId";
  public static final String ATTR_BLOCK_NUMBER = "blockNumber";
  public static final String ATTR_ERROR_MESSAGE = "errorMessage";

  @DynamoKey
  @DynamoAttribute(attributeName = ATTR_CHAIN_ID)
  private String chainId;

  @DynamoKey(dynamoKeyType = DynamoKey.RANGE)
  @DynamoAttribute(attributeName = ATTR_BLOCK_NUMBER, attributeType = "N")
  private Long blockNumber;

  @DynamoAttribute(attributeName = ATTR_ERROR_MESSAGE)
  private String errorMessage;
}
