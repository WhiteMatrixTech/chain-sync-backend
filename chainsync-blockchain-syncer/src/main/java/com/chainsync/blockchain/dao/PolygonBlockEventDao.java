package com.chainsync.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.chainsync.blockchain.model.EthereumBlockEvent;
import com.chainsync.blockchain.model.EventOrmManager;
import com.chainsync.dynamodb.dao.BaseQueryDao;
import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author: ZhangKai
 */
@Log4j2
@Component
public class PolygonBlockEventDao extends BaseQueryDao<EthereumBlockEvent> {

  public PolygonBlockEventDao(
      @Qualifier(EventOrmManager.POLYGON_EVENT_ORM_MANAGER)
          DynamoDBTableOrmManager<EthereumBlockEvent> ormManager,
      DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }
}
