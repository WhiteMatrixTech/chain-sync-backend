package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.dynamodb.orm.DynamoDBItemAggregator;
import com.chainsync.dynamodb.orm.DynamoToObjectAggregator;
import com.chainsync.dynamodb.orm.FieldConverter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import lombok.SneakyThrows;
import lombok.Value;

@Value
public class AutoFieldConverter implements FieldConverter {

  @Value
  public static class FieldDynamoDBFieldPair {

    Field objectField;
    String dynamoDBField;
  }

  DynamoToObjectAggregator dynamoToObjectAggregator;
  DynamoDBItemAggregator dynamoDBItemAggregator;
  FieldDynamoDBFieldPair fieldPair;
  Method setter;
  Method getter;

  @SneakyThrows
  @Override
  public Item convertFieldAndAddToDBItem(final Object object, final Item item) {
    return dynamoDBItemAggregator.aggregate(
        fieldPair.getDynamoDBField(), getter.invoke(object), item);
  }

  @Override
  public Object convertDBFieldAndAddToObject(final Object object, final Item item) {
    return dynamoToObjectAggregator.aggregate(object, setter, item, fieldPair.getDynamoDBField());
  }
}
