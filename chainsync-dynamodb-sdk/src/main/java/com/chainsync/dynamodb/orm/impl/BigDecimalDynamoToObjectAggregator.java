package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.dynamodb.orm.DynamoToObjectAggregator;
import java.lang.reflect.Method;
import lombok.SneakyThrows;

public class BigDecimalDynamoToObjectAggregator implements DynamoToObjectAggregator {

  @SneakyThrows
  @Override
  public Object aggregate(Object object, Method setter, Item item, String dynamoField) {
    Object value = item.getNumber(dynamoField);
    if (value != null) {
      setter.invoke(object, value);
    }
    return object;
  }
}
