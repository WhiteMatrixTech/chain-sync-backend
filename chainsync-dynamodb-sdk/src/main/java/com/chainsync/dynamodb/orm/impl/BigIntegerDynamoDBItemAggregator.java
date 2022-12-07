package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.dynamodb.orm.DynamoDBItemAggregator;
import java.math.BigInteger;

/**
 * BigInteger to DynamoDB item.
 */
public class BigIntegerDynamoDBItemAggregator implements DynamoDBItemAggregator {

  @Override
  public Item aggregate(final String attributeName, final Object element, final Item target) {
    if (element != null) {
      target.withNumber(attributeName, (BigInteger) element);
    }
    return target;
  }
}
