package com.matrix.etl.module;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
import com.matrix.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;
import com.matrix.etl.config.DynamoDbConfig;
import com.matrix.etl.dao.ETHTransactionDao;
import com.matrix.etl.model.EthereumBlockEvent;
import com.matrix.etl.model.Task;
import com.matrix.etl.model.TaskDef;
import com.matrix.etl.model.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author richard
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

  @Bean("tokenOrmManager")
  public DynamoDBTableOrmManager<Token> tokenOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(dynamoDBConfig.getTokenTableName(), Token.class);
  }

  @Bean("ethereumEventOrmManager")
  public DynamoDBTableOrmManager<EthereumBlockEvent> getEthereumEventOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getEthereumEventTableName(), EthereumBlockEvent.class);
  }

  @Bean
  public ETHTransactionDao ethTransactionDao(final DynamoDB dynamoDB) {
    return new ETHTransactionDao(dynamoDBConfig.getEthTransactionTableName(), dynamoDB);
  }
}
