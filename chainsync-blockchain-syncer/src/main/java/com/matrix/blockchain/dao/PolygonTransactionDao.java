package com.matrix.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.blockchain.model.BlockTransaction;
import com.matrix.dynamodb.dao.BaseQueryDao;
import com.matrix.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;

public class PolygonTransactionDao extends BaseQueryDao<BlockTransaction> {

  public PolygonTransactionDao(final String tableName, final DynamoDB dynamoDB) {
    super(new AnnotatedDynamoDBTableOrmManager<>(tableName, BlockTransaction.class), dynamoDB);
  }
}
