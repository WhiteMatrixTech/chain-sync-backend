package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.dynamodb.orm.DynamoToObjectAggregator;
import java.lang.reflect.Method;
import java.time.Instant;
import lombok.SneakyThrows;

public class InstantDynamoToObjectAggregator implements DynamoToObjectAggregator {

  @SneakyThrows
  @Override
  public Object aggregate(Object object, Method setter, Item item, String dynamoField) {
    String instantString = item.getString(dynamoField);
    if (instantString != null) {
      Object value = Instant.parse(instantString);
      setter.invoke(object, value);
    }
    return object;
  }
}
