package com.matrix.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.matrix.common.model.TokenId;
import com.matrix.dynamodb.orm.DynamoToObjectAggregator;
import java.lang.reflect.Method;
import lombok.SneakyThrows;

/**
 * TokenId aggregator
 */
public class TokenIdDynamoToObjectAggregator implements DynamoToObjectAggregator {

  @SneakyThrows
  @Override
  public Object aggregate(Object object, Method setter, Item item, String dynamoField) {
    if (item.hasAttribute(dynamoField) && !item.isNull(dynamoField)) {
      String value = item.getString(dynamoField);
      if (value != null) {
        setter.invoke(object, TokenId.fromHexString(value));
      }
    }

    return object;
  }
}
