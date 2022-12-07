package com.chainsync.metadata.dao;

import static com.chainsync.metadata.model.NftCollection.ATTR_COLLECTION_STATUS;

import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.util.CollectionUtils;
import com.chainsync.common.model.Address;
import com.chainsync.metadata.model.InitCollectionDTO;
import com.chainsync.dynamodb.dao.BaseQueryDao;
import com.chainsync.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;
import com.chainsync.metadata.model.CollectionStatus;
import com.chainsync.metadata.model.NftCollection;
import com.chainsync.metadata.model.NftMetadataField;
import com.chainsync.metadata.orm.MetadataSchemaFieldConverter;
import com.chainsync.metadata.util.EndpointUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;

/**
 * @author yangjian
 * @date 2021/12/30 AM 10:23
 */
@Log4j2
public class NftCollectionDao extends BaseQueryDao<NftCollection> {

  private final String env;

  public NftCollectionDao(final String tableName, final DynamoDB dynamoDB, final String env) {
    super(
        new AnnotatedDynamoDBTableOrmManager<>(
            tableName,
            NftCollection.class,
            Map.of(NftCollection.ATTR_METADATA_SCHEMA, new MetadataSchemaFieldConverter())),
        dynamoDB);
    this.env = env;
  }

  public NftCollection initCollection(final InitCollectionDTO initCollectionDTO) {
    final Address contractAddress = initCollectionDTO.getContractAddress();
    String chainId = contractAddress.getChainId().toString();
    NftCollection collection = NftCollection.builder()
        .contractAddress(contractAddress)
        .appId(initCollectionDTO.getAppId())
        .collectionName(
            initCollectionDTO.getCollectionName() != null
                ? initCollectionDTO.getCollectionName()
                : chainId + "_NFT_" + contractAddress.getNormalizedAddress())
        .description(
            initCollectionDTO.getDescription() != null
                ? initCollectionDTO.getDescription()
                : chainId + "NFT Collection of contract"
                    + contractAddress.getNormalizedAddress())
        .collectionStatus(CollectionStatus.CONTRACT_CREATED)
        .templateId(initCollectionDTO.getTemplateId())
        .deploymentTxHash(initCollectionDTO.getDeploymentTxHash())
        .walletId(initCollectionDTO.getWalletId())
        .metadataEndpoint(
            EndpointUtil.build1SyncMetadataEndpoint(
                env, initCollectionDTO.getAppId(), contractAddress.toString()))
        .createAt(Instant.now())
        .build();
    log.info("[NftCollectionDao.initCollection] add collection: {}", collection);
    return putItem(collection);
  }

  public List<NftCollection> getNftCollections(final boolean withSchema) {
    final List<NftCollection> nftCollections = scan();
    if (withSchema) {
      return nftCollections.stream()
          .filter(nftCollection -> CollectionUtils.isNullOrEmpty(nftCollection.getMetadataSchema()))
          .collect(Collectors.toList());
    }
    return nftCollections;
  }

  public List<NftCollection> getNftCollectionsByAppId(final String appId) {
    return this.queryByPartitionKeyOnGsi(NftCollection.ATTR_APP_ID, appId);
  }

  public NftCollection updateCollectionMetadata(
      final Address contractAddress, final String collectionName, final String description) {
    final List<AttributeUpdate> updates = new ArrayList<>();
    if (collectionName != null) {
      updates.add(new AttributeUpdate(NftCollection.ATTR_COLLECTION_NAME).put(collectionName));
    }
    if (description != null) {
      updates.add(new AttributeUpdate(NftCollection.ATTR_DESCRIPTION).put(description));
    }
    return updateItem(contractAddress.toString(), updates, null);
  }

  public NftCollection getNftCollection(final Address contractAddress) {
    return getItem(contractAddress.toString());
  }

  public NftCollection getCollectionWithMetadataSchema(final Address contractAddress) {
    final NftCollection nftCollection = getNftCollection(contractAddress);
    if (nftCollection == null) {
      throw new IllegalArgumentException(
          String.format("NFT collection of address %s does not exist", contractAddress));
    }
    if (!CollectionStatus.SCHEMA_CREATED.equals(nftCollection.getCollectionStatus())) {
      throw new IllegalArgumentException("Please set the schema first for " + contractAddress);
    }
    return nftCollection;
  }

  public NftCollection putMetadataSchema(
      final Address contractAddress, final List<NftMetadataField> metadataSchema) {
    final NftCollection nftCollection =
        NftCollection.builder()
            .contractAddress(contractAddress)
            .metadataSchema(metadataSchema)
            .build();
    // TODO check the schema later
    return this.updateItem(
        nftCollection.getContractAddress().toString(),
        List.of(
            nftCollection.convertSchemaToStringAttributeUpdate(),
            new AttributeUpdate(ATTR_COLLECTION_STATUS)
                .put(CollectionStatus.SCHEMA_CREATED.name())),
        null);
  }

  public NftCollection updateCollection(final NftCollection nftCollection) {
    // update basic info except metadata schema
    return this.updateItem(
        nftCollection.getContractAddress().toString(),
        nftCollection.convertToAttributeUpdateList(),
        null);
  }

  public NftCollection updateDefaultImageUrl(final Address contractAddress, final String imageUrl) {
    // put basic info except metadata schema
    return updateItem(
        contractAddress.toString(),
        Collections.singletonList(
            new AttributeUpdate(NftCollection.ATTR_DEFAULT_IMAGE).put(imageUrl)),
        null);
  }
}
