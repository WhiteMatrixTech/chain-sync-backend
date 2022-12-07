package com.chainsync.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.chainsync.blockchain.model.BlockSuccess;
import com.chainsync.dynamodb.dao.BaseQueryDao;
import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * @author luyuanheng
 */
@Log4j2
@Component
public class BlockSuccessDao extends BaseQueryDao<BlockSuccess> {

  public BlockSuccessDao(
      final DynamoDBTableOrmManager<BlockSuccess> ormManager, final DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }
}
