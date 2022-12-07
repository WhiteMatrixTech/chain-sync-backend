package com.chainsync.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.chainsync.blockchain.model.BlockTransaction;
import com.chainsync.dynamodb.dao.BaseQueryDao;
import com.chainsync.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;

public class PolygonTransactionDao extends BaseQueryDao<BlockTransaction> {

  public PolygonTransactionDao(final String tableName, final DynamoDB dynamoDB) {
    super(new AnnotatedDynamoDBTableOrmManager<>(tableName, BlockTransaction.class), dynamoDB);
  }
}
