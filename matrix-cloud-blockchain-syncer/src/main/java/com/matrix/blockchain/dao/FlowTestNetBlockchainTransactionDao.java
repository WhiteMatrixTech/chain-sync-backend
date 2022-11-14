package com.matrix.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
import com.matrix.marketplace.blockchain.dao.BlockchainTransactionDao;
import com.matrix.marketplace.blockchain.model.BlockchainTransaction;
import lombok.extern.log4j.Log4j2;

/**
 * @author: ZhangKai
 */
@Log4j2
public class FlowTestNetBlockchainTransactionDao extends BlockchainTransactionDao {

  public FlowTestNetBlockchainTransactionDao(
      DynamoDBTableOrmManager<BlockchainTransaction> ormManager, DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }
}
