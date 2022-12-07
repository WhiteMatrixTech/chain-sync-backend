package com.chainsync.dynamodb.orm;

import com.amazonaws.services.dynamodbv2.document.Item;
import java.lang.reflect.Method;

public interface DynamoToObjectAggregator {

  Object aggregate(Object object, Method setter, Item item, String dynamoField);
}
