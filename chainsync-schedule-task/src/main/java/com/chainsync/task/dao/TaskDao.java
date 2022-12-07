package com.chainsync.task.dao;

import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.chainsync.dynamodb.dao.BaseQueryDao;
import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
import com.chainsync.task.model.Task;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author luyuanheng
 */
@Log4j2
@Component
public class TaskDao extends BaseQueryDao<Task> {

  public TaskDao(final DynamoDBTableOrmManager<Task> ormManager, final DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }

  public void updateTask(final Task task) {
    List<AttributeUpdate> list = new ArrayList<>();
    list.add(new AttributeUpdate(Task.ATTR_LAST_EXECUTE_TIME).put(task.getLastExecuteTime()));
    list.add(new AttributeUpdate(Task.ATTR_STATUS).put(task.getStatus()));
    if (StringUtils.isNotBlank(task.getErrorInfo())) {
      list.add(new AttributeUpdate(Task.ATTR_ERROR_INFO).put(task.getErrorInfo()));
    }
    this.updateItem(task.getTaskName(), task.getTaskId(), list, null);
  }
}
