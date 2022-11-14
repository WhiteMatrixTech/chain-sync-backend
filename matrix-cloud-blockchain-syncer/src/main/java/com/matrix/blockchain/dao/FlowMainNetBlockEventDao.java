package com.matrix.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.blockchain.model.EventOrmManager;
import com.matrix.blockchain.model.FlowBlockEvent;
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
public class FlowMainNetBlockEventDao extends BaseQueryDao<FlowBlockEvent> {

  public FlowMainNetBlockEventDao(
      @Qualifier(EventOrmManager.FLOW_MAIN_NET_EVENT_ORM_MANAGER)
          DynamoDBTableOrmManager<FlowBlockEvent> ormManager,
      DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }
}
