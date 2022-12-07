package com.chainsync.etl.model;

import com.chainsync.dynamodb.annotation.DynamoAttribute;
import com.chainsync.dynamodb.annotation.DynamoGSIKey;
import com.chainsync.dynamodb.annotation.DynamoKey;
import com.chainsync.dynamodb.annotation.DynamoTable;
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
@DynamoTable(globalSecondaryIndices = Token.INDEX_OWNER)
public class Token {

  public static final String ATTR_ADDRESS = "address";
  public static final String ATTR_TOKEN_ID = "tokenId";
  public static final String ATTR_OWNER = "owner";

  public static final String ATTR_TOKEN_METADATA_URI = "tokenMetadataURI";

  public static final String ATTR_TOKEN_METADATA_RAW = "tokenMetadataRaw";

  public static final String INDEX_OWNER = "index_owner";

  @DynamoKey
  @DynamoAttribute(attributeName = ATTR_ADDRESS)
  private String address;

  @DynamoKey(dynamoKeyType = DynamoKey.RANGE)
  @DynamoAttribute(attributeName = ATTR_TOKEN_ID)
  private String tokenId;

  @DynamoGSIKey(dynamoGSINames = INDEX_OWNER)
  @DynamoAttribute(attributeName = ATTR_OWNER)
  private String owner;

  @DynamoAttribute(attributeName = ATTR_TOKEN_METADATA_URI)
  private String tokenMetadataURI;

  @DynamoAttribute(attributeName = ATTR_TOKEN_METADATA_RAW)
  private String tokenMetadataRaw;
}
