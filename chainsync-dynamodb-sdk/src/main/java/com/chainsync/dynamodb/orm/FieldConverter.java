package com.chainsync.dynamodb.orm;

import com.amazonaws.services.dynamodbv2.document.Item;

public interface FieldConverter {

  Item convertFieldAndAddToDBItem(final Object object, final Item item);

  Object convertDBFieldAndAddToObject(final Object object, final Item item);
}
