package com.matrix.etl.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.ScanFilter;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.matrix.dynamodb.dao.BaseQueryDao;
import com.matrix.dynamodb.dao.CursorQueryDao;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
import com.matrix.etl.model.Token;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author reimia
 */
@Component
public class TokenDao extends CursorQueryDao<Token> {

  @Autowired
  public TokenDao(@Qualifier("tokenOrmManager") DynamoDBTableOrmManager<Token> ormManager,
      DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }

  public List<Token> scanWithLimit(int limit) {
    return this.scan(new ScanSpec().withMaxResultSize(limit)
        .withScanFilters(new ScanFilter(Token.ATTR_TOKEN_METADATA_RAW).exists()));
  }
}
