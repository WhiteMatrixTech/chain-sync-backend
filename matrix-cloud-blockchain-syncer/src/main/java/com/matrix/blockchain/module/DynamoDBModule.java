package com.matrix.blockchain.module;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.blockchain.config.DynamoDbConfig;
import com.matrix.blockchain.dao.FlowMainNetBlockchainTransactionDao;
import com.matrix.blockchain.dao.FlowTestNetBlockchainTransactionDao;
import com.matrix.blockchain.dao.TransactionTipDao;
import com.matrix.blockchain.model.BlockFailed;
import com.matrix.blockchain.model.BlockOffset;
import com.matrix.blockchain.model.BlockSuccess;
import com.matrix.blockchain.model.BlockTip;
import com.matrix.blockchain.model.EthereumBlockEvent;
import com.matrix.blockchain.model.EventOrmManager;
import com.matrix.blockchain.model.FlowBlockEvent;
import com.matrix.blockchain.model.SyncError;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
import com.matrix.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;
import com.matrix.marketplace.blockchain.model.BlockchainTransaction;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author luyuanheng
 */
@Configuration
public class DynamoDBModule {

  @Resource private DynamoDbConfig dynamoDBConfig;

  @Bean(EventOrmManager.RINKEBY_EVENT_ORM_MANAGER)
  public DynamoDBTableOrmManager<EthereumBlockEvent> getRinkebyEventOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getRinkebyEventTableName(), EthereumBlockEvent.class);
  }

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

  @Bean(EventOrmManager.FLOW_TEST_NET_EVENT_ORM_MANAGER)
  public DynamoDBTableOrmManager<FlowBlockEvent> getFlowTestNetEventOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getFlowTestNetEventTableName(), FlowBlockEvent.class);
  }

  @Bean(EventOrmManager.FLOW_MAIN_NET_EVENT_ORM_MANAGER)
  public DynamoDBTableOrmManager<FlowBlockEvent> getFlowMainNetEventOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getFlowMainNetEventTableName(), FlowBlockEvent.class);
  }

  @Bean(EventOrmManager.FLOW_TEST_NET_TRANSACTION_ORM_MANAGER)
  public DynamoDBTableOrmManager<BlockchainTransaction> getFlowTestNetTransactionOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getFlowTestNetTransactionTableName(), BlockchainTransaction.class);
  }

  @Bean(EventOrmManager.FLOW_MAIN_NET_TRANSACTION_ORM_MANAGER)
  public DynamoDBTableOrmManager<BlockchainTransaction> getFlowMainNetTransactionOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        this.dynamoDBConfig.getFlowMainNetTransactionTableName(), BlockchainTransaction.class);
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

  @Bean("flowTestNetBlockchainTransactionDao")
  public FlowTestNetBlockchainTransactionDao getFlowTestNetBlockchainTransactionDao(
      final DynamoDB dynamoDB) {
    return new FlowTestNetBlockchainTransactionDao(getFlowTestNetTransactionOrmManager(), dynamoDB);
  }

  @Bean("flowMainNetBlockchainTransactionDao")
  public FlowMainNetBlockchainTransactionDao flowMainNetBlockchainTransactionDao(
      final DynamoDB dynamoDB) {
    return new FlowMainNetBlockchainTransactionDao(getFlowMainNetTransactionOrmManager(), dynamoDB);
  }
}
