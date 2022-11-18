package com.matrix.blockchain.module;

import com.matrix.blockchain.config.DynamoDbConfig;
import com.matrix.blockchain.model.BlockFailed;
import com.matrix.blockchain.model.BlockOffset;
import com.matrix.blockchain.model.BlockSuccess;
import com.matrix.blockchain.model.BlockTip;
import com.matrix.blockchain.model.EthereumBlockEvent;
import com.matrix.blockchain.model.EthereumBlockTransaction;
import com.matrix.blockchain.model.EventOrmManager;
import com.matrix.blockchain.model.EthereumBlockInfo;
import com.matrix.blockchain.model.SyncError;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
import com.matrix.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author luyuanheng
 */
@Configuration
public class DynamoDBModule {

  @Resource private DynamoDbConfig dynamoDBConfig;


  @Bean(EventOrmManager.ETHEREUM_EVENT_ORM_MANAGER)
  public DynamoDBTableOrmManager<EthereumBlockEvent> getEthereumEventOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getEthereumEventTableName(), EthereumBlockEvent.class);
  }

  @Bean(EventOrmManager.MUMBAI_EVENT_ORM_MANAGER)
  public DynamoDBTableOrmManager<EthereumBlockEvent> getMumbaiEventOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getMumbaiEventTableName(), EthereumBlockEvent.class);
  }

  @Bean(EventOrmManager.POLYGON_EVENT_ORM_MANAGER)
  public DynamoDBTableOrmManager<EthereumBlockEvent> getPolygonEventOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getPolygonEventTableName(), EthereumBlockEvent.class);
  }

  @Bean("blockchainTipOrmManager")
  public DynamoDBTableOrmManager<BlockTip> getBlockchainTipOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getBlockchainTipTableName(), BlockTip.class);
  }

  @Bean("blockchainOffsetOrmManager")
  public DynamoDBTableOrmManager<BlockOffset> getBlockchainOffsetOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getBlockchainOffsetTableName(), BlockOffset.class);
  }

  @Bean("blockSuccessOrmManager")
  public DynamoDBTableOrmManager<BlockSuccess> getBlockSuccessOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getBlockSuccessTableName(), BlockSuccess.class);
  }

  @Bean("blockFailedOrmManager")
  public DynamoDBTableOrmManager<BlockFailed> getBlockFailedOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getBlockFailedTableName(), BlockFailed.class);
  }

  @Bean("syncErrorOrmManager")
  public DynamoDBTableOrmManager<SyncError> getSyncErrorOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getSyncErrorTableName(), SyncError.class);
  }

  @Bean("ethereumBlockInfoOrmManager")
  public DynamoDBTableOrmManager<EthereumBlockInfo> getEthereumBlockInfoOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getBlockInfoTableName(), EthereumBlockInfo.class);
  }

  @Bean("ethereumBlockTransactionOrmManager")
  public DynamoDBTableOrmManager<EthereumBlockTransaction> getEthereumBlockTransactionOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getBlockTransactionTableName(), EthereumBlockTransaction.class);
  }

}
