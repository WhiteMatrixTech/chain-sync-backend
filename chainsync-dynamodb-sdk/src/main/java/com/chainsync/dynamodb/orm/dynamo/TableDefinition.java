package com.chainsync.dynamodb.orm.dynamo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.BillingMode;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TableDefinition {

  private String tableName;
  private IndexDefinition hashAndSortKey;
  private Map<String, IndexDefinition> globalSecondaryIndices;
  private Set<DynamoDBAttributeType> nonKeyedAttributeTypes;

  public CreateTableRequest toCreateTableRequest() {
    final List<AttributeDefinition> attributeDefinitions = hashAndSortKey.toAttributeDefinition();
    final CreateTableRequest createTableRequest =
        new CreateTableRequest()
            .withTableName(tableName)
            .withKeySchema(hashAndSortKey.toKeySchemaElements())
            .withBillingMode(BillingMode.PAY_PER_REQUEST);
    for (final Entry<String, IndexDefinition> entry : globalSecondaryIndices.entrySet()) {
      createTableRequest.withGlobalSecondaryIndexes(
          new GlobalSecondaryIndex()
              .withIndexName(entry.getKey())
              .withKeySchema(entry.getValue().toKeySchemaElements())
              .withProjection(entry.getValue().toGsiProjection()));
      attributeDefinitions.addAll(entry.getValue().toAttributeDefinition());
    }
    final HashSet<String> uniqueAttributeSet = new HashSet<>();
    attributeDefinitions.removeIf(e -> !uniqueAttributeSet.add(e.getAttributeName()));

    createTableRequest.withAttributeDefinitions(attributeDefinitions);
    return createTableRequest;
  }
}
