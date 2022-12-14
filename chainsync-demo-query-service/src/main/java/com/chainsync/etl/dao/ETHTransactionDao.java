package com.chainsync.etl.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.ScanFilter;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.chainsync.etl.model.BlockTransaction;
import com.chainsync.dynamodb.dao.BaseQueryDao;
import com.chainsync.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;
import java.util.List;

public class ETHTransactionDao extends BaseQueryDao<BlockTransaction> {

  public ETHTransactionDao(final String tableName, final DynamoDB dynamoDB) {
    super(new AnnotatedDynamoDBTableOrmManager<>(tableName, BlockTransaction.class), dynamoDB);
  }

  public List<BlockTransaction> scanWithLimit(final int limit) {
    return scan(
        new ScanSpec()
            .withMaxResultSize(limit)
            .withScanFilters(
                new ScanFilter(BlockTransaction.ATTR_TRANSACTION_HASH)
                    .ne(BlockTransaction.ROOT_TRANSACTION_HASH)));
  }

  public List<BlockTransaction> queryIndexWithLimit(
      final String indexName, final String partitionKeyValue, final int limit) {
    final QuerySpec querySpec =
        new QuerySpec()
            .withHashKey(
                this.getTableDefinition()
                    .getGlobalSecondaryIndices()
                    .get(indexName)
                    .getHashKey()
                    .getKeyName(),
                partitionKeyValue)
            .withMaxResultSize(limit);

    return queryOutcomeToItems(
        this.getDynamoDB()
            .getTable(this.getTableDefinition().getTableName())
            .getIndex(indexName)
            .query(querySpec));
  }

  public List<BlockTransaction> queryByPartitionKeyWithLimit(
      final Object partitionKeyValue, final int limit) {
    final QuerySpec querySpec =
        new QuerySpec()
            .withHashKey(
                this.getTableDefinition().getHashAndSortKey().getHashKey().getKeyName(),
                partitionKeyValue)
            .withMaxResultSize(limit);

    return queryOutcomeToItems(
        this.getDynamoDB().getTable(this.getTableDefinition().getTableName()).query(querySpec));
  }
}
