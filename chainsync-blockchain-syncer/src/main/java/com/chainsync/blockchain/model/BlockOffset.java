package com.chainsync.blockchain.model;

import com.chainsync.dynamodb.annotation.DynamoAttribute;
import com.chainsync.dynamodb.annotation.DynamoKey;
import com.chainsync.dynamodb.annotation.DynamoTable;
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
public class BlockOffset {

  public static final String ATTR_CHAIN_ID = "chainId";
  public static final String ATTR_START = "start";
  public static final String ATTR_END = "end";
  public static final String ATTR_OFFSET = "offset";

  @DynamoKey
  @DynamoAttribute(attributeName = ATTR_CHAIN_ID)
  private String chainId;

  @DynamoKey(dynamoKeyType = DynamoKey.RANGE)
  @DynamoAttribute(attributeName = ATTR_START, attributeType = "N")
  private Long start;

  @DynamoAttribute(attributeName = ATTR_END, attributeType = "N")
  private Long end;

  @DynamoAttribute(attributeName = ATTR_OFFSET, attributeType = "N")
  private Long offset;
}
