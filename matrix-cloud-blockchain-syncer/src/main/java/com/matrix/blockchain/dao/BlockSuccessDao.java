package com.matrix.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.blockchain.model.BlockSuccess;
import com.matrix.blockchain.model.BlockTip;
import com.matrix.dynamodb.dao.BaseQueryDao;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
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
