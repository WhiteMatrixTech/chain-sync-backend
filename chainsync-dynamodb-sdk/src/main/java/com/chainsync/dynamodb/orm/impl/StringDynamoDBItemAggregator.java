package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.dynamodb.orm.DynamoDBItemAggregator;

public class StringDynamoDBItemAggregator implements DynamoDBItemAggregator {

  @Override
  public Item aggregate(final String attributeName, final Object element, final Item target) {
    if (element != null) {
      target.withString(attributeName, (String) element);
    }
    return target;
  }
}
