package com.chainsync.dynamodb.orm;

import com.amazonaws.services.dynamodbv2.document.Item;

public interface DynamoDBItemAggregator {

  Item aggregate(String name, Object element, Item target);
}
