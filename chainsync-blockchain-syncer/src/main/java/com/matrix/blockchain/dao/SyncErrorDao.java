package com.matrix.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.blockchain.model.SyncError;
import com.matrix.dynamodb.dao.CursorQueryDao;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
import org.springframework.stereotype.Component;

/**
 * @author shuyizhang
 */
@Component
public class SyncErrorDao extends CursorQueryDao<SyncError> {

  public SyncErrorDao(
      final DynamoDBTableOrmManager<SyncError> ormManager, final DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }
}
