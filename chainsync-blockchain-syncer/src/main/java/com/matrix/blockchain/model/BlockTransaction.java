package com.matrix.blockchain.model;

import com.matrix.dynamodb.annotation.DynamoAttribute;
import com.matrix.dynamodb.annotation.DynamoGSIKey;
import com.matrix.dynamodb.annotation.DynamoKey;
import com.matrix.dynamodb.annotation.DynamoTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author reimia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoTable(
    globalSecondaryIndices = {
      BlockTransaction.INDEX_TRANSACTION_HASH,
      BlockTransaction.INDEX_FROM,
      BlockTransaction.INDEX_TO,
      BlockTransaction.INDEX_STATUS
    })
public class BlockTransaction {

  public static final String ROOT_TRANSACTION_HASH = "0x";

  public static final String ATTR_BLOCK_NUMBER = "blockNumber";
  public static final String ATTR_TRANSACTION_HASH = "transactionHash";
  public static final String ATTR_FROM = "from";
  public static final String ATTR_TO = "to";
  public static final String ATTR_RAW_DATA = "rawData";

  public static final String ATTR_STATUS = "status";

  public static final String ATTR_FROM_LIST = "fromList";
  public static final String ATTR_TO_LIST = "toList";

  public static final String INDEX_TRANSACTION_HASH = "index_transactionHash";
  public static final String INDEX_FROM = "index_from";
  public static final String INDEX_TO = "index_to";
  public static final String INDEX_STATUS = "index_status";

  @DynamoKey
  @DynamoAttribute(attributeName = ATTR_BLOCK_NUMBER, attributeType = "N")
  private Long blockNumber;

  /** if transactionHash == "0x", it's rawData include all transaction info */
  @DynamoKey(dynamoKeyType = DynamoKey.RANGE)
  @DynamoGSIKey(dynamoGSINames = INDEX_TRANSACTION_HASH)
  @DynamoAttribute(attributeName = ATTR_TRANSACTION_HASH)
  private String transactionHash;

  @DynamoGSIKey(dynamoGSINames = INDEX_FROM)
  @DynamoAttribute(attributeName = ATTR_FROM)
  private String from;

  @DynamoGSIKey(dynamoGSINames = INDEX_TO)
  @DynamoAttribute(attributeName = ATTR_TO)
  private String to;

  @DynamoAttribute(attributeName = ATTR_RAW_DATA)
  private String rawData;

  @DynamoGSIKey(dynamoGSINames = INDEX_STATUS)
  @DynamoAttribute(attributeName = ATTR_STATUS)
  private String status;

  @DynamoAttribute(attributeName = ATTR_FROM_LIST)
  private String fromList;

  @DynamoAttribute(attributeName = ATTR_TO_LIST)
  private String toList;
}
