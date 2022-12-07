package com.matrix.blockchain.model;

import com.matrix.common.model.ChainType;
import com.matrix.dynamodb.annotation.DynamoAttribute;
import com.matrix.dynamodb.annotation.DynamoKey;
import com.matrix.dynamodb.annotation.DynamoTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xinyao(alvin) sun
 * @date 2021-01-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoTable(globalSecondaryIndices = {"chainType"})
public class ContractTemplate {
  public static final String ATTR_TEMPLATE_ID = "templateId";
  public static final String ATTR_DESCRIPTION = "description";
  public static final String ATTR_CHAIN_TYPE = "chainType";
  public static final String ATTR_BINARY = "binary";
  public static final String ATTR_ABI = "abi";
  public static final String ATTR_VISIBLE = "visible";

  @DynamoKey
  @DynamoAttribute(attributeName = ATTR_TEMPLATE_ID)
  private String templatedId;

  @DynamoAttribute(attributeName = ATTR_DESCRIPTION)
  private String description;

  @DynamoKey(dynamoGSIName = ATTR_CHAIN_TYPE)
  @DynamoAttribute(attributeName = ATTR_CHAIN_TYPE)
  private ChainType chainType;

  @DynamoAttribute(attributeName = ATTR_BINARY)
  private String binary;

  @DynamoAttribute(attributeName = ATTR_ABI)
  private String abi;

  /** -1 is invisible */
  @DynamoAttribute(attributeName = ATTR_VISIBLE, attributeType = "N")
  private int visible;
}
