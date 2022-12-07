package com.chainsync.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.chainsync.blockchain.model.EthereumBlockInfo;
import com.chainsync.dynamodb.dao.BaseQueryDao;
import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author richard
 */
@Log4j2
@Component
public class EthereumBlockInfoDao extends BaseQueryDao<EthereumBlockInfo> {

  public EthereumBlockInfoDao(
      @Qualifier("ethereumBlockInfoOrmManager")
          DynamoDBTableOrmManager<EthereumBlockInfo> ormManager,
      DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }
}
