package com.matrix.blockchain.model;

import com.matrix.dynamodb.annotation.DynamoAttribute;
import com.matrix.dynamodb.annotation.DynamoKey;
import com.matrix.dynamodb.annotation.DynamoTable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author shuyizhang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoTable
public class SyncError {

  public static final String ERROR_TYPE = "errorType";
  public static final String ERROR_DETAIL = "errorDetail";
  public static final String UPDATED_AT = "updatedAt";
  public static final String BLOCK_NUMBER = "blockNumber";
  public static final String CHAIN_TYPE = "chainType";

  @DynamoAttribute(attributeName = ERROR_TYPE)
  private String errorType;

  @DynamoKey(dynamoKeyType = DynamoKey.RANGE)
  @DynamoAttribute(attributeName = BLOCK_NUMBER, attributeType = "N")
  private Long blockNumber;

  @DynamoKey
  @DynamoAttribute(attributeName = CHAIN_TYPE)
  private String chainType;

  @DynamoAttribute(attributeName = ERROR_DETAIL)
  private String errorDetail;

  @DynamoAttribute(attributeName = UPDATED_AT)
  private Instant updatedAt;
}
