package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.dynamodb.orm.DynamoToObjectAggregator;
import java.lang.reflect.Method;
import lombok.SneakyThrows;

public class BooleanDynamoToObjectAggregator implements DynamoToObjectAggregator {

  @SneakyThrows
  @Override
  public Object aggregate(Object object, Method setter, Item item, String dynamoField) {
    if (item.hasAttribute(dynamoField) && !item.isNull(dynamoField)) {
      Boolean value = item.getBOOL(dynamoField);
      if (value != null) {
        setter.invoke(object, value);
      }
    }

    return object;
  }
}
