package com.chainsync.dynamodb.orm.dynamo;

import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeyDefinition {

  private String keyName;
  private ScalarAttributeType keyAttributeType;
}
