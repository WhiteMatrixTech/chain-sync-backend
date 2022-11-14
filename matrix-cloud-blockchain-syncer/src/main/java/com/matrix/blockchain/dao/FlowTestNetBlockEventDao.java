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
 * @author luyuanheng
 */
@Log4j2
@Component
public class FlowTestNetBlockEventDao extends BaseQueryDao<FlowBlockEvent> {

  public FlowTestNetBlockEventDao(
      @Qualifier(EventOrmManager.FLOW_TEST_NET_EVENT_ORM_MANAGER)
          final DynamoDBTableOrmManager<FlowBlockEvent> ormManager,
      final DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }
}
