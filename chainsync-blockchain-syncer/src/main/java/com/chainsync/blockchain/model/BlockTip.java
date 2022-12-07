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
public class BlockTip {

  public static final String ATTR_CHAIN_ID = "chainId";
  public static final String ATTR_BLOCK_NUMBER = "blockNumber";

  @DynamoKey
  @DynamoAttribute(attributeName = ATTR_CHAIN_ID)
  private String chainId;

  @DynamoAttribute(attributeName = ATTR_BLOCK_NUMBER, attributeType = "N")
  private Long blockNumber;
}
