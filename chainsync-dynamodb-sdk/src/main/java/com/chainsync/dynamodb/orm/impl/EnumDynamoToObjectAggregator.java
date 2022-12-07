package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.dynamodb.orm.DynamoToObjectAggregator;
import java.lang.reflect.Method;
import lombok.SneakyThrows;

public class EnumDynamoToObjectAggregator implements DynamoToObjectAggregator {

  @SneakyThrows
  @Override
  public Object aggregate(Object object, Method setter, Item item, String dynamoField) {
    Object value = item.getString(dynamoField);
    if (value != null) {
      final Enum enumValue =
          Enum.valueOf((Class<Enum>) setter.getParameterTypes()[0], value.toString());
      setter.invoke(object, enumValue);
    }
    return object;
  }
}
