package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.dynamodb.orm.DynamoToObjectAggregator;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;

public class LongListDynamoToObjectAggregator implements DynamoToObjectAggregator {

  @SneakyThrows
  @Override
  public Object aggregate(Object object, Method setter, Item item, String dynamoField) {
    List<Long> valueAsList =
        item.getList(dynamoField).stream()
            .map(bigDecimalVal -> ((BigDecimal) bigDecimalVal).longValueExact())
            .collect(Collectors.toList());
    if (!valueAsList.isEmpty()) {
      setter.invoke(object, valueAsList);
    }
    return object;
  }
}
