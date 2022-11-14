package com.matrix.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.matrix.common.model.Address;
import com.matrix.dynamodb.orm.DynamoToObjectAggregator;
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
