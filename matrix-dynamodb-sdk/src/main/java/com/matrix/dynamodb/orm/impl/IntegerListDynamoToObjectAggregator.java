package com.matrix.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.matrix.dynamodb.orm.DynamoToObjectAggregator;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;

public class IntegerListDynamoToObjectAggregator implements DynamoToObjectAggregator {

  @SneakyThrows
  @Override
  public Object aggregate(Object object, Method setter, Item item, String dynamoField) {
    List<Integer> valueAsList =
        item.getList(dynamoField).stream()
            .map(bigDecimalVal -> ((BigDecimal) bigDecimalVal).intValueExact())
            .collect(Collectors.toList());
    if (!valueAsList.isEmpty()) {
      setter.invoke(object, valueAsList);
    }
    return object;
  }
}
