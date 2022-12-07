package com.chainsync.dynamodb.orm.dynamo;

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class IndexDefinition {

  private KeyDefinition hashKey;
  private KeyDefinition rangeKey;
  private Long readCapacity;
  private Long writeCapacity;
  private Set<String> gsiProjectionAttributes;
  private boolean keysOnly;

  public List<KeySchemaElement> toKeySchemaElements() {
    final List<KeySchemaElement> keySchemaElements = new ArrayList<>();
    keySchemaElements.add(new KeySchemaElement(hashKey.getKeyName(), KeyType.HASH));
    if (rangeKey != null) {
      keySchemaElements.add(new KeySchemaElement(rangeKey.getKeyName(), KeyType.RANGE));
    }
    return keySchemaElements;
  }

  public List<AttributeDefinition> toAttributeDefinition() {
    final List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
    attributeDefinitions.add(
        new AttributeDefinition(hashKey.getKeyName(), hashKey.getKeyAttributeType()));
    if (rangeKey != null) {
      attributeDefinitions.add(
          new AttributeDefinition(rangeKey.getKeyName(), rangeKey.getKeyAttributeType()));
    }
    return attributeDefinitions;
  }

  public Projection toGsiProjection() {
    final Projection projection = new Projection();
    if (keysOnly) {
      projection.withProjectionType(ProjectionType.KEYS_ONLY);
    } else if (gsiProjectionAttributes == null || gsiProjectionAttributes.isEmpty()) {
      projection.withProjectionType(ProjectionType.ALL);
    } else {
      projection.withProjectionType(ProjectionType.INCLUDE);
      projection.withNonKeyAttributes(gsiProjectionAttributes);
    }
    return projection;
  }
}
