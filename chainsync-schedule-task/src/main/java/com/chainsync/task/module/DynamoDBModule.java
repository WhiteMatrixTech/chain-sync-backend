package com.chainsync.task.module;

import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
import com.chainsync.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;
import com.chainsync.task.config.DynamoDbConfig;
import com.chainsync.task.model.Task;
import com.chainsync.task.model.TaskDef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author luyuanheng
 */
@Configuration
public class DynamoDBModule {

  @Autowired private DynamoDbConfig dynamoDBConfig;

  @Bean("taskDefOrmManager")
  public DynamoDBTableOrmManager<TaskDef> getTaskDefOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getTaskDefTableName(), TaskDef.class);
  }

  @Bean("taskOrmManager")
  public DynamoDBTableOrmManager<Task> getTaskOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getTaskTableName(), Task.class);
  }
}
