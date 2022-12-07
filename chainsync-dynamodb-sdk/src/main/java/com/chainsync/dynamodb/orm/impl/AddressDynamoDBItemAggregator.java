package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.dynamodb.orm.DynamoDBItemAggregator;

/**
 * @author yangjian
 * @date 2022/1/26 AM 11:15
 */
public class AddressDynamoDBItemAggregator implements DynamoDBItemAggregator {

  @Override
  public Item aggregate(final String name, final Object element, final Item target) {
    if (element != null) {
      target.withString(name, element.toString());
    }
    return target;
  }
}
