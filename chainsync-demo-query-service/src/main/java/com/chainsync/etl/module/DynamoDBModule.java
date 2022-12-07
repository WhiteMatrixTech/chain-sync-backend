package com.chainsync.etl.module;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
import com.chainsync.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;
import com.chainsync.etl.config.DynamoDbConfig;
import com.chainsync.etl.dao.ETHTransactionDao;
import com.chainsync.etl.model.BlockTip;
import com.chainsync.etl.model.EthereumBlockEvent;
import com.chainsync.etl.model.Task;
import com.chainsync.etl.model.TaskDef;
import com.chainsync.etl.model.Token;
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

  @Bean("blockchainTipOrmManager")
  public DynamoDBTableOrmManager<BlockTip> getBlockchainTipOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getBlockchainTipTableName(), BlockTip.class);
  }
}
