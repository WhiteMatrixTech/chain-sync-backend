package com.matrix.etl.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.dynamodb.dao.CursorQueryDao;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
import com.matrix.etl.model.Task;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * @author richard
 */
@Log4j2
@Component
public class TaskDao extends CursorQueryDao<Task> {

  public TaskDao(final DynamoDBTableOrmManager<Task> ormManager, final DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }
}
