package com.matrix.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.matrix.dynamodb.orm.DynamoToObjectAggregator;
import java.lang.reflect.Method;
import java.util.List;
import lombok.SneakyThrows;

public class StringListDynamoToObjectAggregator implements DynamoToObjectAggregator {

  @SneakyThrows
  @Override
  public Object aggregate(Object object, Method setter, Item item, String dynamoField) {
    List<String> valueAsList = item.getList(dynamoField);
    if (valueAsList != null) {
      setter.invoke(object, valueAsList);
    }
    return object;
  }
}
