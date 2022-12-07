package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.common.model.TokenId;
import com.chainsync.dynamodb.orm.DynamoDBItemAggregator;

/**
 * TokenId aggregator
 */
public class TokenIdDynamoDBItemAggregator implements DynamoDBItemAggregator {

  @Override
  public Item aggregate(final String attributeName, final Object element, final Item target) {
    if (element != null) {
      target.withString(attributeName, ((TokenId) element).toUint64HexString());
    }
    return target;
  }
}
