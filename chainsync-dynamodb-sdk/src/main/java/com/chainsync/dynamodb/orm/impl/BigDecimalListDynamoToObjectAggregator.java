package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.dynamodb.orm.DynamoToObjectAggregator;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import lombok.SneakyThrows;

public class BigDecimalListDynamoToObjectAggregator implements DynamoToObjectAggregator {

  @SneakyThrows
  @Override
  public Object aggregate(Object object, Method setter, Item item, String dynamoField) {
    final List<BigDecimal> valueAsList = item.getList(dynamoField);
    setter.invoke(object, valueAsList);
    return object;
  }
}
