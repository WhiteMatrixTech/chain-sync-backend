package com.chainsync.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.amazonaws.services.dynamodbv2.document.QueryFilter;
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition;
import com.chainsync.blockchain.model.BlockchainTransaction;
import com.chainsync.blockchain.model.BlockchainTransaction.Receipt;
import com.chainsync.blockchain.model.RawTransactionStatus;
import com.chainsync.blockchain.orm.TransactionPayloadFieldConverter;
import com.chainsync.blockchain.orm.TransactionReceiptFieldConverter;
import com.chainsync.common.model.Address;
import com.chainsync.common.model.TransactionHash;
import com.chainsync.dynamodb.dao.BaseQueryDao;
import com.chainsync.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DAO for blockchain transaction
 *
 * @author ShenYang
 */
public class TransactionDao extends BaseQueryDao<BlockchainTransaction> {

  public TransactionDao(final String tableName, final DynamoDB dynamoDB) {
    super(
        new AnnotatedDynamoDBTableOrmManager<>(
            tableName,
            BlockchainTransaction.class,
            Map.of(BlockchainTransaction.ATTR_PAYLOAD, new TransactionPayloadFieldConverter(),
                BlockchainTransaction.ATTR_RECEIPT, new TransactionReceiptFieldConverter())
        ),
        dynamoDB
    );
  }

  /**
   * create new transaction
   *
   * @param tx transaction object
   * @return transaction
   */
  public BlockchainTransaction saveTransaction(BlockchainTransaction tx) {
    return putItem(tx);
  }

  /**
   * update transaction's status/receipt/updateTime, the update condition is db's current status(
   * queried status must equal db's status when updating).
   *
   * @param transactionHash TransactionHash's toString()
   * @param targetStatus    status to update
   * @param receipt         receipt to update
   * @param statusCondition the status as update condition
   * @return transaction
   */
  public BlockchainTransaction updateTransaction(
      String transactionHash,
      RawTransactionStatus targetStatus,
      Receipt receipt,
      RawTransactionStatus statusCondition) {

    // field converter is not suitable for update operation
    String receiptStr = new TransactionReceiptFieldConverter().receiptToString(receipt);

    return updateItem(transactionHash,
        List.of(
            new AttributeUpdate(BlockchainTransaction.ATTR_STATUS).put(targetStatus.toString()),
            new AttributeUpdate(BlockchainTransaction.ATTR_RECEIPT).put(receiptStr),
            new AttributeUpdate(BlockchainTransaction.ATTR_UPDATE_TIME).put(Instant.now().toString())
        ),
        List.of(
            new Expected(BlockchainTransaction.ATTR_STATUS).eq(statusCondition.toString())
        )
    );
  }

  /**
   * query a transaction
   *
   * @param transactionHash transaction hash
   * @return transaction
   */
  public BlockchainTransaction getTransaction(TransactionHash transactionHash) {
    return getItem(transactionHash.toString());
  }

  /**
   * query list by from address & transaction status
   *
   * @param from   from address
   * @param appId  app id
   * @param status transaction status
   * @return transaction list
   */
  public List<BlockchainTransaction> getTransactionListByFromAddress(
      Address from,
      String appId,
      RawTransactionStatus status) {

    RangeKeyCondition rangeKeyCondition = new RangeKeyCondition(BlockchainTransaction.ATTR_STATUS);
    rangeKeyCondition.eq(status.toString());

    return super.queryByPartitionKeyAndSortKeyOnGsi(
        BlockchainTransaction.INDEX_FROM_WITH_STATUS,
        from.toString(),
        rangeKeyCondition,
        new QueryFilter(BlockchainTransaction.ATTR_APP_ID).eq(appId)
    );
  }
}
