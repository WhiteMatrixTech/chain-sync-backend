package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.common.model.TransactionHash;
import com.chainsync.dynamodb.orm.DynamoToObjectAggregator;
import java.lang.reflect.Method;
import lombok.SneakyThrows;

/**
 * DynamoDB item to TransactionHash field
 *
 * @author ShenYang
 */
public class TransactionHashDynamoToObjectAggregator implements DynamoToObjectAggregator {

  @SneakyThrows
  @Override
  public Object aggregate(
      final Object object, final Method setter, final Item item, final String dynamoField) {
    final String value = item.getString(dynamoField);
    if (value != null) {
      final TransactionHash transactionHash = TransactionHash.fromCanonicalHash(value);
      setter.invoke(object, transactionHash);
    }
    return object;
  }
}
