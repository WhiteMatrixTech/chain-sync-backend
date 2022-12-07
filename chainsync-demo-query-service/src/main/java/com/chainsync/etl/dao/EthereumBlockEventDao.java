package com.chainsync.etl.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.chainsync.etl.model.EthereumBlockEvent;
import com.chainsync.dynamodb.dao.BaseQueryDao;
import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author reimia
 */
@Log4j2
@Component
public class EthereumBlockEventDao extends BaseQueryDao<EthereumBlockEvent> {

  public EthereumBlockEventDao(
      @Qualifier("ethereumEventOrmManager")
          final DynamoDBTableOrmManager<EthereumBlockEvent> ormManager,
      final DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }

  public List<EthereumBlockEvent> scanWithLimit(final int limit) {
    return scan(new ScanSpec().withMaxResultSize(limit));
  }
}
