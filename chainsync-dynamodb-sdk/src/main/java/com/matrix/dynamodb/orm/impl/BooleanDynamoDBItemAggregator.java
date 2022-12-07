package com.matrix.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.matrix.dynamodb.orm.DynamoDBItemAggregator;

public class BooleanDynamoDBItemAggregator implements DynamoDBItemAggregator {

  @Override
  public Item aggregate(final String attributeName, final Object element, final Item target) {
    if (element != null) {
      target.withBoolean(attributeName, (Boolean) element);
    }
    return target;
  }
}
