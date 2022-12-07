package com.chainsync.dynamodb.orm.impl;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.google.common.base.Preconditions;
import com.chainsync.dynamodb.annotation.DynamoAttribute;
import com.chainsync.dynamodb.annotation.DynamoGSIKey;
import com.chainsync.dynamodb.annotation.DynamoHashKey;
import com.chainsync.dynamodb.annotation.DynamoKey;
import com.chainsync.dynamodb.annotation.DynamoRangeKey;
import com.chainsync.dynamodb.annotation.DynamoTable;
import com.chainsync.dynamodb.orm.DynamoDBItemAggregators;
import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
import com.chainsync.dynamodb.orm.DynamoToObjectAggregators;
import com.chainsync.dynamodb.orm.FieldConverter;
import com.chainsync.dynamodb.orm.dynamo.IndexDefinition;
import com.chainsync.dynamodb.orm.dynamo.KeyDefinition;
import com.chainsync.dynamodb.orm.dynamo.TableDefinition;
import com.chainsync.dynamodb.util.AnnotationUtil;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public class AnnotatedDynamoDBTableOrmManager<T> implements DynamoDBTableOrmManager<T> {

  private final Class<T> clazz;
  private Map<String, FieldConverter> fieldConverters;
  private TableDefinition tableDefinition;

  public AnnotatedDynamoDBTableOrmManager(final String tableName, final Class<T> clazz) {
    this.clazz = clazz;
    buildTableDefinitionAndFieldConverters(tableName, clazz);
  }

  public AnnotatedDynamoDBTableOrmManager(
      final String tableName,
      final Class<T> clazz,
      final Map<String, FieldConverter> fieldConverterOverrides) {
    this.clazz = clazz;
    buildTableDefinitionAndFieldConverters(tableName, clazz);
    for (final Entry<String, FieldConverter> entry : fieldConverterOverrides.entrySet()) {
      if (fieldConverters.containsKey(entry.getKey())) {
        fieldConverters.put(entry.getKey(), entry.getValue());
      } else {
        throw new IllegalArgumentException("Wrong fieldConverterOverrides key: " + entry.getKey());
      }
    }
  }

  @SneakyThrows
  @Override
  public T toObject(final Item item) {
    if (item == null) {
      return null;
    }
    final T object = clazz.getConstructor().newInstance();
    for (final FieldConverter fieldConverter : fieldConverters.values()) {
      if (fieldConverter == null) {
        continue;
      }
      fieldConverter.convertDBFieldAndAddToObject(object, item);
    }
    return object;
  }

  @SneakyThrows
  @Override
  public Item toDynamoDBItem(final T object) {
    if (object == null) {
      return null;
    }
    final Item item = new Item();
    for (final FieldConverter fieldConverter : fieldConverters.values()) {
      if (fieldConverter == null) {
        continue;
      }
      fieldConverter.convertFieldAndAddToDBItem(object, item);
    }
    return item;
  }

  @Override
  public TableDefinition getTableDefinition() {
    return tableDefinition;
  }

  @SneakyThrows
  public void buildTableDefinitionAndFieldConverters(final String tableName, final Class<T> clazz) {
    Preconditions.checkArgument(
        clazz.isAnnotationPresent(DynamoTable.class), "@DynamoTable annotation does not exist");

    this.fieldConverters = new HashMap<>();

    final DynamoTable dynamoTable = clazz.getAnnotation(DynamoTable.class);
    final Map<String, IndexDefinition> globalSecondaryIndices = new HashMap<>();
    Arrays.stream(dynamoTable.globalSecondaryIndices())
        .distinct()
        .filter(StringUtils::isNoneBlank)
        .forEach(gsiName -> globalSecondaryIndices.put(gsiName, new IndexDefinition()));
    final IndexDefinition tableIndex = new IndexDefinition();

    Arrays.stream(clazz.getDeclaredFields())
        .forEach(
            field -> {
              // the reason to add multiple if blocks is for further customization
              if (field.isAnnotationPresent(DynamoAttribute.class)) {
                final DynamoAttribute dynamoAttribute = field.getAnnotation(DynamoAttribute.class);
                this.fieldConverters.put(
                    dynamoAttribute.attributeName(),
                    buildFieldConverter(clazz, field, dynamoAttribute.attributeName()));
              }

              if (field.isAnnotationPresent(DynamoKey.class)) {
                addIndex(field, tableIndex, globalSecondaryIndices);
              }

              if (field.isAnnotationPresent(DynamoGSIKey.class)) {
                addGSIIndex(field, globalSecondaryIndices);
              }

              if (field.isAnnotationPresent(DynamoHashKey.class)) {
                addHashIndex(field, globalSecondaryIndices);
              }

              if (field.isAnnotationPresent(DynamoRangeKey.class)) {
                addRangeIndex(field, globalSecondaryIndices);
              }

              if (field.isAnnotationPresent(DynamoAttribute.class)) {
                addGSIInclude(field, globalSecondaryIndices);
              }
            });

    this.tableDefinition =
        TableDefinition.builder()
            .tableName(tableName)
            .hashAndSortKey(tableIndex)
            .globalSecondaryIndices(globalSecondaryIndices)
            .build();
  }

  private void addIndex(
      final Field field,
      final IndexDefinition tableIndex,
      final Map<String, IndexDefinition> globalSecondaryIndices) {
    final DynamoKey dynamoKey = field.getAnnotation(DynamoKey.class);
    final boolean isHashKey = DynamoKey.HASH.equals(dynamoKey.dynamoKeyType());
    final boolean isRangeKey = DynamoKey.RANGE.equals(dynamoKey.dynamoKeyType());
    final DynamoAttribute dynamoAttribute = field.getAnnotation(DynamoAttribute.class);
    final KeyDefinition keyDefinition =
        new KeyDefinition(
            dynamoAttribute.attributeName(),
            ScalarAttributeType.fromValue(dynamoAttribute.attributeType()));

    IndexDefinition index = tableIndex;
    if (!StringUtils.isBlank(dynamoKey.dynamoGSIName())) {
      if (globalSecondaryIndices.containsKey(dynamoKey.dynamoGSIName())) {
        index = globalSecondaryIndices.get(dynamoKey.dynamoGSIName());
      } else {
        throw new IllegalArgumentException(
            "@DynamoKey's keyType GSI name must be in table's annotation");
      }
    }
    if (isHashKey) {
      index.setHashKey(keyDefinition);
      index.setReadCapacity(Long.parseLong(dynamoKey.readCapacity()));
      index.setWriteCapacity(Long.parseLong(dynamoKey.writeCapacity()));
    } else if (isRangeKey) {
      index.setRangeKey(keyDefinition);
    } else {
      throw new IllegalArgumentException("DynamoKey's keyType must be either hashKey or rangeKey");
    }
  }

  private void addGSIIndex(
      final Field field, final Map<String, IndexDefinition> globalSecondaryIndices) {
    final DynamoGSIKey annotation = field.getAnnotation(DynamoGSIKey.class);
    final boolean isHashKey = DynamoGSIKey.HASH.equals(annotation.dynamoKeyType());
    final boolean isRangeKey = DynamoGSIKey.RANGE.equals(annotation.dynamoKeyType());

    final DynamoAttribute dynamoAttribute = field.getAnnotation(DynamoAttribute.class);
    final KeyDefinition keyDefinition =
        new KeyDefinition(
            dynamoAttribute.attributeName(),
            ScalarAttributeType.fromValue(dynamoAttribute.attributeType()));
    for (String dynamoGSIName : annotation.dynamoGSINames()) {
      final IndexDefinition index;
      if (globalSecondaryIndices.containsKey(dynamoGSIName)) {
        index = globalSecondaryIndices.get(dynamoGSIName);
      } else {
        throw new IllegalArgumentException(
            "@DynamoGSIKey's keyType GSI name must be in table's annotation");
      }
      if (isHashKey) {
        index.setHashKey(keyDefinition);
        index.setKeysOnly(annotation.isKeysOnly());
        index.setReadCapacity(Long.parseLong(annotation.readCapacity()));
        index.setWriteCapacity(Long.parseLong(annotation.writeCapacity()));
      } else if (isRangeKey) {
        index.setRangeKey(keyDefinition);
      } else {
        throw new IllegalArgumentException(
            "DynamoGSIKey's keyType must be either hashKey or rangeKey");
      }
    }
  }

  private void addHashIndex(
      final Field field, final Map<String, IndexDefinition> globalSecondaryIndices) {
    final DynamoHashKey annotation = field.getAnnotation(DynamoHashKey.class);

    final DynamoAttribute dynamoAttribute = field.getAnnotation(DynamoAttribute.class);
    final KeyDefinition keyDefinition =
        new KeyDefinition(
            dynamoAttribute.attributeName(),
            ScalarAttributeType.fromValue(dynamoAttribute.attributeType()));
    for (String dynamoGSIName : annotation.dynamoGSINames()) {
      final IndexDefinition index;
      if (globalSecondaryIndices.containsKey(dynamoGSIName)) {
        index = globalSecondaryIndices.get(dynamoGSIName);
      } else {
        throw new IllegalArgumentException(
            "@DynamoHashKey's keyType GSI name must be in table's annotation");
      }
      index.setHashKey(keyDefinition);
      index.setKeysOnly(annotation.isKeysOnly());
      index.setReadCapacity(Long.parseLong(annotation.readCapacity()));
      index.setWriteCapacity(Long.parseLong(annotation.writeCapacity()));
    }
  }

  private void addRangeIndex(
      final Field field, final Map<String, IndexDefinition> globalSecondaryIndices) {
    final DynamoRangeKey annotation = field.getAnnotation(DynamoRangeKey.class);

    final DynamoAttribute dynamoAttribute = field.getAnnotation(DynamoAttribute.class);
    final KeyDefinition keyDefinition =
        new KeyDefinition(
            dynamoAttribute.attributeName(),
            ScalarAttributeType.fromValue(dynamoAttribute.attributeType()));
    for (String dynamoGSIName : annotation.dynamoGSINames()) {
      final IndexDefinition index;
      if (globalSecondaryIndices.containsKey(dynamoGSIName)) {
        index = globalSecondaryIndices.get(dynamoGSIName);
      } else {
        throw new IllegalArgumentException(
            "@DynamoRangeKey's keyType GSI name must be in table's annotation");
      }
      index.setRangeKey(keyDefinition);
    }
  }

  private void addGSIInclude(
      final Field field, final Map<String, IndexDefinition> globalSecondaryIndices) {
    final DynamoAttribute dynamoAttribute = field.getAnnotation(DynamoAttribute.class);
    if (!Arrays.equals(dynamoAttribute.includeInGsi(), new String[] {""})) {
      Arrays.stream(dynamoAttribute.includeInGsi())
          .forEach(
              indexName -> {
                if (globalSecondaryIndices.containsKey(indexName)) {
                  final IndexDefinition indexDefinition = globalSecondaryIndices.get(indexName);
                  if (indexDefinition.getGsiProjectionAttributes() == null) {
                    indexDefinition.setGsiProjectionAttributes(new HashSet<>());
                  }
                  indexDefinition.getGsiProjectionAttributes().add(dynamoAttribute.attributeName());
                } else {
                  throw new IllegalArgumentException(
                      "@DynamoKey's keyType GSI name must be in table's annotation");
                }
              });
    }
  }

  @SneakyThrows
  private AutoFieldConverter buildFieldConverter(
      final Class<?> clazz, final Field field, final String dynamoDBField) {
    // Put a null value for the field

    final Class<?> fieldType = field.getType().isEnum() ? Enum.class : field.getType();
    Class<?> genericType = null;

    if (fieldType.equals(List.class)) {
      final ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
      genericType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
    } else if (fieldType.equals(Map.class)) {
      final ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
      // get [1] since [0] is the key's type
      genericType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
    }

    if (DynamoToObjectAggregators.getAggregator(fieldType, genericType) == null
        || DynamoDBItemAggregators.getAggregator(fieldType) == null) {
      return null;
    }

    return new AutoFieldConverter(
        DynamoToObjectAggregators.getAggregator(fieldType, genericType),
        DynamoDBItemAggregators.getAggregator(fieldType),
        new AutoFieldConverter.FieldDynamoDBFieldPair(field, dynamoDBField),
        AnnotationUtil.findSetter(clazz, field.getName()),
        AnnotationUtil.findGetter(clazz, field.getName()));
  }
}
