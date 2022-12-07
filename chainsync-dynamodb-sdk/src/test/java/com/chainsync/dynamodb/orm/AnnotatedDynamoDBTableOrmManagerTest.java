package com.chainsync.dynamodb.orm;

import static com.chainsync.dynamodb.orm.AnnotatedDynamoDBTableOrmManagerTest.TestObject.GSI_HASH;
import static com.chainsync.dynamodb.orm.AnnotatedDynamoDBTableOrmManagerTest.TestObject.GSI_RANGE;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.google.common.collect.ImmutableMap;
import com.chainsync.dynamodb.annotation.DynamoAttribute;
import com.chainsync.dynamodb.annotation.DynamoHashKey;
import com.chainsync.dynamodb.annotation.DynamoKey;
import com.chainsync.dynamodb.annotation.DynamoRangeKey;
import com.chainsync.dynamodb.annotation.DynamoTable;
import com.chainsync.dynamodb.orm.dynamo.IndexDefinition;
import com.chainsync.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AnnotatedDynamoDBTableOrmManagerTest {

  @Test
  void testConvert() {
    final TestObject testObject = new TestObject(1, "1", ImmutableMap.of("1", "1"), "1");
    final AnnotatedDynamoDBTableOrmManager<TestObject> annotatedObjectToDynamoDBItemConverter =
        new AnnotatedDynamoDBTableOrmManager<TestObject>("test-table", TestObject.class);

    Item item = annotatedObjectToDynamoDBItemConverter.toDynamoDBItem(testObject);
    System.out.println(item.toJSONPretty());

    TestObject testObjectRoundTrip =
        (TestObject) annotatedObjectToDynamoDBItemConverter.toObject(item);
    System.out.println(testObjectRoundTrip.toString());

    Map<String, IndexDefinition> globalSecondaryIndices =
        annotatedObjectToDynamoDBItemConverter.getTableDefinition().getGlobalSecondaryIndices();
    Assertions.assertTrue(globalSecondaryIndices.containsKey(GSI_HASH));
    Assertions.assertTrue(globalSecondaryIndices.containsKey(GSI_RANGE));
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @DynamoTable(globalSecondaryIndices = {GSI_HASH, GSI_RANGE})
  public static class TestObject {

    public static final String GSI_HASH = "gsi_hash";
    public static final String GSI_RANGE = "gsi_range";

    @DynamoKey(dynamoKeyType = DynamoKey.HASH)
    @DynamoAttribute(attributeName = "attr1", attributeType = "N")
    private Integer attr1;

    @DynamoAttribute(attributeName = "attr2", attributeType = "S")
    private String attr2;

    @DynamoAttribute(attributeName = "attr3", attributeType = "M")
    private Map<String, String> attr3;

    @DynamoAttribute(attributeName = "attr4", attributeType = "S")
    @DynamoHashKey(dynamoGSINames = {GSI_HASH})
    @DynamoRangeKey(dynamoGSINames = {GSI_RANGE})
    private String attr4;
  }
}
