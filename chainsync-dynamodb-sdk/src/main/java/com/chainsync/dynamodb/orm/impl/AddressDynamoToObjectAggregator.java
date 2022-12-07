package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.common.model.Address;
import com.chainsync.dynamodb.orm.DynamoToObjectAggregator;
import java.lang.reflect.Method;
import lombok.SneakyThrows;
/**
 * @author yangjian
 * @date 2022/1/26 AM 11:15
 */
public class AddressDynamoToObjectAggregator implements DynamoToObjectAggregator {

  @SneakyThrows
  @Override
  public Object aggregate(
      final Object object, final Method setter, final Item item, final String dynamoField) {
    final String value = item.getString(dynamoField);
    if (value != null) {
      final Address address = Address.fromFullAddress(value);
      setter.invoke(object, address);
    }
    return object;
  }
}
