package com.matrix.dynamodb.orm;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.matrix.dynamodb.orm.dynamo.TableDefinition;

/**
 * @author shuyi
 */
public interface DynamoDBTableOrmManager<T> {

  /**
   * convert a DynamoDB {@link Item} to a templated object T
   *
   * @param item the DDB item to convert fromt
   * @return the object
   */
  T toObject(Item item);

  /**
   * convert templated object T to a DynamoDB {@link Item}
   *
   * @param object the object to convert from
   * @return the converted DynamoDB object
   */
  Item toDynamoDBItem(T object);

  TableDefinition getTableDefinition();
}
