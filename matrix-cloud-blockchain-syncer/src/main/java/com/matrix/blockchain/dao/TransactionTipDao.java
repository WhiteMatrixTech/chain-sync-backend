package com.matrix.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.blockchain.model.TransactionTip;
import com.matrix.dynamodb.dao.BaseQueryDao;
import com.matrix.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;

/**
 * Transaction tip dao.
 *
 * @author ShenYang
 */
public class TransactionTipDao extends BaseQueryDao<TransactionTip> {
  public TransactionTipDao(final String tableName, final DynamoDB dynamoDB) {
    super(new AnnotatedDynamoDBTableOrmManager<>(tableName, TransactionTip.class), dynamoDB);
  }
}
