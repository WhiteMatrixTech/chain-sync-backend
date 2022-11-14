package com.matrix.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.matrix.dynamodb.orm.DynamoToObjectAggregator;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import lombok.SneakyThrows;

public class LongDynamoToObjectAggregator implements DynamoToObjectAggregator {

  @SneakyThrows
  @Override
  public Object aggregate(Object object, Method setter, Item item, String dynamoField) {
    BigDecimal value = item.getNumber(dynamoField);
    if (value != null) {
      setter.invoke(object, value.longValue());
    }
    return object;
  }
}
