package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.dynamodb.orm.DynamoDBItemAggregator;
import java.math.BigDecimal;

public class BigDecimalDynamoDBItemAggregator implements DynamoDBItemAggregator {

  @Override
  public Item aggregate(final String attributeName, final Object element, final Item target) {
    if (element != null) {
      target.withNumber(attributeName, (BigDecimal) element);
    }
    return target;
  }
}
