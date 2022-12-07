package com.chainsync.blockchain.module;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.chainsync.blockchain.config.DynamoDbConfig;
import com.chainsync.blockchain.model.BlockFailed;
import com.chainsync.blockchain.model.BlockOffset;
import com.chainsync.blockchain.model.BlockSuccess;
import com.chainsync.blockchain.model.BlockTip;
import com.chainsync.blockchain.model.EthereumBlockEvent;
import com.chainsync.blockchain.model.EthereumBlockInfo;
import com.chainsync.blockchain.model.SyncError;
import com.chainsync.blockchain.dao.BscTransactionDao;
import com.chainsync.blockchain.dao.EthereumTransactionDao;
import com.chainsync.blockchain.dao.PolygonTransactionDao;
import com.chainsync.blockchain.model.EventOrmManager;
import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
import com.chainsync.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;
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

  @Bean(EventOrmManager.BSC_EVENT_ORM_MANAGER)
  public DynamoDBTableOrmManager<EthereumBlockEvent> getMumbaiEventOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getBscEventTableName(), EthereumBlockEvent.class);
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

  @Bean("ethereumTransactionDao")
  public EthereumTransactionDao ethTransactionDao(final DynamoDB dynamoDB) {
    return new EthereumTransactionDao(dynamoDBConfig.getEthTransactionTableName(), dynamoDB);
  }

  @Bean("bscTransactionDao")
  public BscTransactionDao bscTransactionDao(final DynamoDB dynamoDB) {
    return new BscTransactionDao(dynamoDBConfig.getBscTransactionTableName(), dynamoDB);
  }

  @Bean("polygonTransactionDao")
  public PolygonTransactionDao polygonTransactionDao(final DynamoDB dynamoDB) {
    return new PolygonTransactionDao(dynamoDBConfig.getPolygonTransactionTableName(), dynamoDB);
  }
}
