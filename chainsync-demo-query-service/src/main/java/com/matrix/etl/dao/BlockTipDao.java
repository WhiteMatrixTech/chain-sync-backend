package com.matrix.etl.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.dynamodb.dao.BaseQueryDao;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
import com.matrix.etl.model.BlockTip;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * @author luyuanheng
 */
@Log4j2
@Component
public class BlockTipDao extends BaseQueryDao<BlockTip> {

  public BlockTipDao(final DynamoDBTableOrmManager<BlockTip> ormManager, final DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }
}
