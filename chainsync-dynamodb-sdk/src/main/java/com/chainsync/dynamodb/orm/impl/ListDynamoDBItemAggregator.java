package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.dynamodb.orm.DynamoDBItemAggregator;
import java.util.List;

public class ListDynamoDBItemAggregator implements DynamoDBItemAggregator {

  @Override
  public Item aggregate(final String attributeName, final Object element, final Item target) {
    if (element != null) {
      target.withList(attributeName, (List<?>) element);
    }
    return target;
  }
}
