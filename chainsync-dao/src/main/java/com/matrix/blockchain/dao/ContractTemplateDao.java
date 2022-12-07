package com.matrix.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.blockchain.model.ContractTemplate;
import com.matrix.common.model.ChainType;
import com.matrix.dynamodb.dao.BaseQueryDao;
import com.matrix.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;
import java.util.List;

/**
 * @author xinyao(alvin) sun
 * @date 2021-02-03
 */
public class ContractTemplateDao extends BaseQueryDao<ContractTemplate> {

  public ContractTemplateDao(final String tableName, final DynamoDB dynamoDB) {
    super(new AnnotatedDynamoDBTableOrmManager<>(tableName, ContractTemplate.class), dynamoDB);
  }

  public List<ContractTemplate> getAllTemplatesByChainType(ChainType chainType) {
    return this.queryByPartitionKeyOnGsi(ContractTemplate.ATTR_CHAIN_TYPE, chainType);
  }
}
