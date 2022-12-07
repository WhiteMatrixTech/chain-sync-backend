package com.chainsync.marketplace.model;

import com.chainsync.marketplace.constants.Constants;
import com.chainsync.marketplace.util.PaddingUtil;
import com.chainsync.dynamodb.annotation.DynamoAttribute;
import com.chainsync.dynamodb.annotation.DynamoKey;
import com.chainsync.dynamodb.annotation.DynamoTable;
import java.time.Instant;
import java.util.List;
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
@DynamoTable(globalSecondaryIndices = {BlockchainTransaction.ATTR_TRANSACTION_HASH})
public class BlockchainTransaction {

  public static final int BLOCK_NUMBER_LEN = 16;
  public static final int LOG_INDEX_LEN = 8;
  public static final int BATCH_SIZE = 10000;
  public static final String ATTR_TRANSACTION_HASH = "transactionHash";
  public static final String ATTR_ITEM_UPDATER_STATUS = "itemUpdaterStatus";

  @DynamoKey
  @DynamoAttribute(attributeName = "id")
  private String id;

  @DynamoKey(dynamoKeyType = DynamoKey.RANGE)
  @DynamoAttribute(attributeName = "transactionKey")
  private String transactionKey;

  @DynamoAttribute(attributeName = "chainType")
  private String chainType;

  @DynamoAttribute(attributeName = "blockNumber", attributeType = "N")
  private Long blockNumber;

  @DynamoAttribute(attributeName = "transactionIndex")
  private Long transactionIndex;

  @DynamoKey(dynamoGSIName = "transactionHash")
  @DynamoAttribute(attributeName = "transactionHash")
  private String transactionHash;

  @DynamoAttribute(attributeName = "events", attributeType = "L")
  private List<String> events;

  @DynamoAttribute(attributeName = "status")
  private String status;

  @DynamoAttribute(attributeName = "updatedAt")
  private Instant updatedAt;

  @DynamoAttribute(attributeName = "itemUpdaterStatus")
  private ItemUpdaterStatus itemUpdaterStatus;

  public void resolveIdAndKey() {
    this.setId(this.getChainType() + Constants.CONNECTOR + (this.getBlockNumber() / BATCH_SIZE));
    this.setTransactionKey(
        PaddingUtil.paddingAndConnect(
            this.getBlockNumber(),
            BLOCK_NUMBER_LEN,
            this.getTransactionIndex(),
            LOG_INDEX_LEN,
            PaddingUtil.ZERO));
  }
}
