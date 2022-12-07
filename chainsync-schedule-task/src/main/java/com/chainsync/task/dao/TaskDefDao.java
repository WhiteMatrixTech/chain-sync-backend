package com.chainsync.task.dao;

import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.chainsync.dynamodb.dao.BaseQueryDao;
import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
import com.chainsync.task.model.TaskDef;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * @author luyuanheng
 */
@Log4j2
@Component
public class TaskDefDao extends BaseQueryDao<TaskDef> {

  public TaskDefDao(final DynamoDBTableOrmManager<TaskDef> ormManager, final DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }

  public void updateTaskDef(final TaskDef taskDef) {
    List<AttributeUpdate> list = new ArrayList<>();
    list.add(new AttributeUpdate(TaskDef.ATTR_TASK_TYPE).put(taskDef.getTaskType()));
    list.add(new AttributeUpdate(TaskDef.ATTR_PARAMS).put(taskDef.getParams()));
    list.add(new AttributeUpdate(TaskDef.ATTR_REPEATED_MODE).put(taskDef.getRepeatedMode()));
    list.add(new AttributeUpdate(TaskDef.ATTR_EXPRESSION).put(taskDef.getExpression()));
    list.add(new AttributeUpdate(TaskDef.ATTR_SYNC).put(taskDef.getSync()));
    list.add(
        new AttributeUpdate(TaskDef.ATTR_LATEST_MODIFY_TIME).put(taskDef.getLatestModifyTime()));
    this.updateItem(taskDef.getTaskName(), list, null);
  }
}
