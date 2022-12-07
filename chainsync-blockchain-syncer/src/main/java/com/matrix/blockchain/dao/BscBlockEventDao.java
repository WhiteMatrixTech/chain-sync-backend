package com.matrix.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.blockchain.model.EthereumBlockEvent;
import com.matrix.blockchain.model.EventOrmManager;
import com.matrix.dynamodb.dao.BaseQueryDao;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author luyuanheng
 */
@Log4j2
@Component
public class BscBlockEventDao extends BaseQueryDao<EthereumBlockEvent> {

  public BscBlockEventDao(
      @Qualifier(EventOrmManager.BSC_EVENT_ORM_MANAGER)
          final DynamoDBTableOrmManager<EthereumBlockEvent> ormManager,
      final DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }
}
