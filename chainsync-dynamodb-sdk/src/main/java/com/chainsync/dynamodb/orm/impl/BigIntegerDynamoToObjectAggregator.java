package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.dynamodb.orm.DynamoToObjectAggregator;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import lombok.SneakyThrows;

/**
 * DynamoDB item to BigInteger
 */
public class BigIntegerDynamoToObjectAggregator implements DynamoToObjectAggregator {

  @SneakyThrows
  @Override
  public Object aggregate(Object object, Method setter, Item item, String dynamoField) {
    BigDecimal value = item.getNumber(dynamoField);
    if (value != null) {
      setter.invoke(object, BigInteger.valueOf(value.longValue()));
    }
    return object;
  }
}
