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
 * @author: ZhangKai
 */
@Log4j2
@Component
public class EthereumBlockEventDao extends BaseQueryDao<EthereumBlockEvent> {

  public EthereumBlockEventDao(
      @Qualifier(EventOrmManager.ETHEREUM_EVENT_ORM_MANAGER)
          DynamoDBTableOrmManager<EthereumBlockEvent> ormManager,
      DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }
}
