package com.chainsync.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.chainsync.blockchain.model.ContractTemplate;
import com.chainsync.common.model.ChainType;
import com.chainsync.dynamodb.dao.BaseQueryDao;
import com.chainsync.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;
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
