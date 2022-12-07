package com.chainsync.metadata.model;

import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.chainsync.common.model.Address;
import com.chainsync.dynamodb.annotation.DynamoAttribute;
import com.chainsync.dynamodb.annotation.DynamoKey;
import com.chainsync.dynamodb.annotation.DynamoTable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

/**
 * @author yangjian
 * @date 2021/12/30 AM 10:17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoTable(
    globalSecondaryIndices = {NftCollection.ATTR_APP_ID, NftCollection.ATTR_COLLECTION_STATUS})
@JsonInclude(Include.NON_NULL)
public class NftCollection {

  public static final String ATTR_APP_ID = "appId";
  public static final String ATTR_CONTRACT_ADDRESS = "contractAddress";
  public static final String ATTR_COLLECTION_NAME = "collectionName";
  public static final String ATTR_COLLECTION_STATUS = "collectionStatus";
  public static final String ATTR_DESCRIPTION = "description";
  public static final String ATTR_DEFAULT_IMAGE = "defaultImage";
  public static final String ATTR_OWNER_ADDRESS = "ownerAddress";
  public static final String ATTR_DEPLOYED_BLOCK_HEIGHT = "deployedBlockHeight";
  public static final String ATTR_DEPLOYED_TRANSACTION_ID = "deployedTransactionId";
  public static final String ATTR_TOKEN_NAME = "tokenName";
  public static final String ATTR_TOKEN_SYMBOL = "tokenSymbol";
  public static final String ATTR_METADATA_SCHEMA = "metadataSchema";
  public static final String ATTR_METADATA_ENDPOINT = "metadataEndpoint";
  public static final String ATTR_TEMPLATE_ID = "templateId";
  public static final String ATTR_DEPLOYMENT_TX_HASH = "deploymentTxHash";
  public static final String ATTR_WALLET_ID = "walletId";
  public static final String ATTR_CREATE_AT = "createAt";

  /** this is not a changeable attribute */
  @JsonIgnore
  @DynamoKey
  @DynamoAttribute(attributeName = ATTR_CONTRACT_ADDRESS)
  private Address contractAddress;

  /** this is not a changeable attribute */
  @DynamoKey(dynamoGSIName = ATTR_APP_ID)
  @DynamoAttribute(attributeName = ATTR_APP_ID)
  private String appId;

  @DynamoKey(dynamoGSIName = ATTR_COLLECTION_STATUS)
  @DynamoAttribute(attributeName = ATTR_COLLECTION_STATUS)
  private CollectionStatus collectionStatus;

  @DynamoAttribute(attributeName = ATTR_COLLECTION_NAME)
  private String collectionName;

  @DynamoAttribute(attributeName = ATTR_DESCRIPTION)
  private String description;

  @DynamoAttribute(attributeName = ATTR_DEFAULT_IMAGE)
  private String defaultImage;

  @DynamoAttribute(attributeName = ATTR_OWNER_ADDRESS)
  private Address ownerAddress;

  /** this is not a changeable attribute */
  @DynamoAttribute(attributeName = ATTR_DEPLOYED_BLOCK_HEIGHT)
  private Long deployedBlockHeight;

  /** this is not a changeable attribute */
  @DynamoAttribute(attributeName = ATTR_DEPLOYED_TRANSACTION_ID)
  private String deployedTransactionId;

  /** this is not a changeable attribute */
  @DynamoAttribute(attributeName = ATTR_TOKEN_NAME)
  private String tokenName;

  /** this is not a changeable attribute */
  @DynamoAttribute(attributeName = ATTR_TOKEN_SYMBOL)
  private String tokenSymbol;

  @DynamoAttribute(attributeName = ATTR_METADATA_SCHEMA)
  private List<NftMetadataField> metadataSchema;

  @DynamoAttribute(attributeName = ATTR_METADATA_ENDPOINT)
  private String metadataEndpoint;

  @DynamoAttribute(attributeName = ATTR_TEMPLATE_ID)
  private String templateId;

  @DynamoAttribute(attributeName = ATTR_DEPLOYMENT_TX_HASH)
  private String deploymentTxHash;

  @DynamoAttribute(attributeName = ATTR_WALLET_ID)
  private String walletId;

  @DynamoAttribute(attributeName = ATTR_CREATE_AT)
  private Instant createAt;

  @SneakyThrows
  public AttributeUpdate convertSchemaToStringAttributeUpdate() {
    if (metadataSchema != null) {
      return new AttributeUpdate(ATTR_METADATA_SCHEMA)
          .put(
              metadataSchema.stream()
                  .map(NftMetadataField::toMapRepresentation)
                  .collect(Collectors.toList()));
    }
    return null;
  }

  public List<AttributeUpdate> convertToAttributeUpdateList() {
    final List<AttributeUpdate> attributeUpdates = new ArrayList<>();
    if (collectionName != null) {
      attributeUpdates.add(new AttributeUpdate(ATTR_COLLECTION_NAME).put(collectionName));
    }
    if (description != null) {
      attributeUpdates.add(new AttributeUpdate(ATTR_DESCRIPTION).put(description));
    }
    if (defaultImage != null) {
      attributeUpdates.add(new AttributeUpdate(ATTR_DEFAULT_IMAGE).put(defaultImage));
    }
    if (ownerAddress != null) {
      attributeUpdates.add(new AttributeUpdate(ATTR_OWNER_ADDRESS).put(ownerAddress));
    }
    return attributeUpdates;
  }

  public String representableName() {
    if (collectionName != null) {
      return collectionName;
    }
    return contractAddress.getNormalizedAddress();
  }

  public String getContractId() {
    return this.contractAddress.toString();
  }
}
