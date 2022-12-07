package com.chainsync.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.chainsync.blockchain.model.SyncError;
import com.chainsync.dynamodb.dao.CursorQueryDao;
import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
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
