package com.matrix.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.blockchain.model.EthereumBlockInfo;
import com.matrix.dynamodb.dao.BaseQueryDao;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
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
