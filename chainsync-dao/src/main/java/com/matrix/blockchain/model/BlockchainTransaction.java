package com.matrix.blockchain.model;

import static com.matrix.blockchain.model.BlockchainTransaction.ATTR_APP_ID;
import static com.matrix.blockchain.model.BlockchainTransaction.ATTR_FROM;
import static com.matrix.blockchain.model.BlockchainTransaction.INDEX_FROM_WITH_STATUS;
import static com.matrix.dynamodb.annotation.DynamoGSIKey.RANGE;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.matrix.common.model.Address;
import com.matrix.common.model.ChainId;
import com.matrix.common.model.TransactionHash;
import com.matrix.dynamodb.annotation.DynamoAttribute;
import com.matrix.dynamodb.annotation.DynamoGSIKey;
import com.matrix.dynamodb.annotation.DynamoKey;
import com.matrix.dynamodb.annotation.DynamoTable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The blockchain transaction
 *
 * @author ShenYang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoTable(globalSecondaryIndices = {ATTR_APP_ID, ATTR_FROM, INDEX_FROM_WITH_STATUS})
public class BlockchainTransaction {

  public static final String ATTR_TRANSACTION_HASH = "transactionHash"; // index
  public static final String ATTR_APP_ID = "appId"; // index
  public static final String ATTR_FROM = "from"; // index
  public static final String ATTR_TO = "to";
  public static final String ATTR_CHAIN_ID = "chainId";
  public static final String ATTR_STATUS = "status";
  public static final String ATTR_PAYLOAD = "payload";
  public static final String ATTR_RECEIPT = "receipt";
  public static final String ATTR_CREATE_TIME = "createTime";
  public static final String ATTR_UPDATE_TIME = "updateTime";

  public static final String INDEX_FROM_WITH_STATUS = "index_from_with_status";

  /**
   * transaction id
   */
  @DynamoKey
  @DynamoAttribute(attributeName = ATTR_TRANSACTION_HASH)
  private TransactionHash transactionHash;

  /**
   * app id
   */
  @DynamoKey(dynamoGSIName = ATTR_APP_ID)
  @DynamoAttribute(attributeName = ATTR_APP_ID)
  private String appId;

  /**
   * address - from
   */
  @DynamoGSIKey(dynamoGSINames = {ATTR_FROM, INDEX_FROM_WITH_STATUS})
  @DynamoAttribute(attributeName = ATTR_FROM)
  @JsonSerialize(using = ToStringSerializer.class)
  private Address from;

  /**
   * address - to
   */
  @DynamoAttribute(attributeName = ATTR_TO)
  @JsonSerialize(using = ToStringSerializer.class)
  private Address to;

  /**
   * chain id
   */
  @DynamoAttribute(attributeName = ATTR_CHAIN_ID)
  private ChainId chainId;

  /**
   * transaction status
   */
  @DynamoGSIKey(dynamoGSINames = {INDEX_FROM_WITH_STATUS}, dynamoKeyType = RANGE)
  @DynamoAttribute(attributeName = ATTR_STATUS)
  private RawTransactionStatus status;

  /**
   * transaction payload
   */
  @DynamoAttribute(attributeName = ATTR_PAYLOAD)
  private Payload payload;

  /**
   * transaction receipt
   */
  @DynamoAttribute(attributeName = ATTR_RECEIPT)
  private Receipt receipt;

  /**
   * create time
   */
  @DynamoAttribute(attributeName = ATTR_CREATE_TIME)
  private Instant creatTime;

  /**
   * update time
   */
  @DynamoAttribute(attributeName = ATTR_UPDATE_TIME)
  private Instant updateTime;

  /**
   * Generic payload type
   */
  public interface Payload {

  }

  /**
   * Generic Receipt type
   */
  public interface Receipt {

  }
}
