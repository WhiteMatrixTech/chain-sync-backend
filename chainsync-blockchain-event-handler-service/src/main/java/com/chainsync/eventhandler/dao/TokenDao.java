package com.chainsync.eventhandler.dao;

import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.chainsync.eventhandler.model.Token;
import com.chainsync.dynamodb.dao.BaseQueryDao;
import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author reimia
 */
@Component
public class TokenDao extends BaseQueryDao<Token> {

  @Autowired
  public TokenDao(@Qualifier("tokenOrmManager") DynamoDBTableOrmManager<Token> ormManager,
      DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }

  public void update(String address, String tokenId, List<AttributeUpdate> attributeUpdates) {
    this.updateItem(address, tokenId, attributeUpdates, null);
  }
}
