package com.matrix.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.blockchain.model.BlockFailed;
import com.matrix.dynamodb.dao.BaseQueryDao;
import com.matrix.dynamodb.dao.CursorQueryDao;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * @author luyuanheng
 */
@Log4j2
@Component
public class BlockFailedDao extends CursorQueryDao<BlockFailed> {

  public BlockFailedDao(
      final DynamoDBTableOrmManager<BlockFailed> ormManager, final DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }
}
