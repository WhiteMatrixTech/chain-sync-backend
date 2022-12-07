package com.chainsync.marketplace.dao;

import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.chainsync.marketplace.constants.Constants;
import com.chainsync.dynamodb.dao.BaseQueryDao;
import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
import com.chainsync.marketplace.model.BlockRange;
import com.chainsync.marketplace.model.BlockchainTransaction;
import com.chainsync.marketplace.util.PaddingUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;

/**
 * @author luyuanheng
 */
@Log4j2
public class BlockchainTransactionDao extends BaseQueryDao<BlockchainTransaction> {

  public BlockchainTransactionDao(
      final DynamoDBTableOrmManager<BlockchainTransaction> ormManager, final DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }

  public List<BlockchainTransaction> queryTransactions(final BlockRange blockRange) {
    final String fromId = getPartitionKey(blockRange.getChainType(), blockRange.getFrom());
    final String toId = getPartitionKey(blockRange.getChainType(), blockRange.getTo());

    final int fromBlockNumberLen = Long.valueOf(blockRange.getFrom()).toString().length();
    final String fromPadding =
        PaddingUtil.ZERO.repeat(BlockchainTransaction.BLOCK_NUMBER_LEN - fromBlockNumberLen);

    final Long to = blockRange.getTo() + 1;
    final String toPadding =
        PaddingUtil.ZERO.repeat(BlockchainTransaction.BLOCK_NUMBER_LEN - to.toString().length());

    final String logIndexPadding = PaddingUtil.ZERO.repeat(BlockchainTransaction.LOG_INDEX_LEN);

    final String rangeFrom = fromPadding + blockRange.getFrom() + logIndexPadding;
    final String rangeTo = toPadding + to + logIndexPadding;

    if (fromId.equals(toId)) {
      // the same partition key
      return queryByRange(fromId, rangeFrom, rangeTo);
    } else {
      // the diff partition key
      final Long mid =
          (blockRange.getTo() / BlockchainTransaction.BATCH_SIZE)
              * BlockchainTransaction.BATCH_SIZE;
      final String midPadding =
          PaddingUtil.ZERO.repeat(BlockchainTransaction.BLOCK_NUMBER_LEN - mid.toString().length());
      final String rangeMid = midPadding + mid + logIndexPadding;

      final List<BlockchainTransaction> transactions = new ArrayList<>();
      transactions.addAll(queryByRange(fromId, rangeFrom, rangeMid));
      transactions.addAll(queryByRange(toId, rangeMid, rangeTo));
      return transactions;
    }
  }

  private String getPartitionKey(final String chainType, final Long blockNumber) {
    return chainType + Constants.CONNECTOR + (blockNumber / BlockchainTransaction.BATCH_SIZE);
  }

  public void updateItemUpdaterStatus(
      final String transactionHash,
      final List<AttributeUpdate> updateList,
      final List<Expected> expectedList) {
    final List<BlockchainTransaction> blockchainTransactions =
        queryByPartitionKeyOnGsi(BlockchainTransaction.ATTR_TRANSACTION_HASH, transactionHash);
    if (!blockchainTransactions.isEmpty()) {
      log.info(
          "[BlockchainTransactionDao] updateItemUpdaterStatus updateList: {}, expectedList: {}, currentTransaction: {}",
          updateList,
          expectedList,
          blockchainTransactions.get(0));
      updateItem(
          blockchainTransactions.get(0).getId(),
          blockchainTransactions.get(0).getTransactionKey(),
          updateList,
          expectedList);
    } else {
      log.error("Can not find transaction by transaction hash: {}", transactionHash);
    }
  }
}
