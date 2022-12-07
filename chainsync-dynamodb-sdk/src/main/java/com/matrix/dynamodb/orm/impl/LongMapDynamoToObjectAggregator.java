package com.matrix.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.matrix.dynamodb.orm.DynamoToObjectAggregator;
import java.lang.reflect.Method;
import java.util.Map;
import lombok.SneakyThrows;

public class LongMapDynamoToObjectAggregator implements DynamoToObjectAggregator {

  @SneakyThrows
  @Override
  public Object aggregate(Object object, Method setter, Item item, String dynamoField) {
    Map<String, Long> valueAsMap = item.getMapOfNumbers(dynamoField, Long.class);
    if (valueAsMap != null) {
      setter.invoke(object, valueAsMap);
    }
    return object;
  }
}
