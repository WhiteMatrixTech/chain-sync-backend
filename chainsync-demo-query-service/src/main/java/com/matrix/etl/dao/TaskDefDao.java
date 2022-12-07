package com.matrix.etl.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.dynamodb.dao.BaseQueryDao;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
import com.matrix.etl.model.TaskDef;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * @author richard
 */
@Log4j2
@Component
public class TaskDefDao extends BaseQueryDao<TaskDef> {

  public TaskDefDao(final DynamoDBTableOrmManager<TaskDef> ormManager, final DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }
}
