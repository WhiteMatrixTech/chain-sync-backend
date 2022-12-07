package com.matrix.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.matrix.dynamodb.orm.DynamoDBItemAggregator;

/**
 * Simple to string DynamoDB item aggregator
 *
 * @author ShenYang
 */
public class ToStringDynamoDBItemAggregator implements DynamoDBItemAggregator {

  @Override
  public Item aggregate(final String name, final Object element, final Item target) {
    if (element != null) {
      target.withString(name, element.toString());
    }
    return target;
  }
}
