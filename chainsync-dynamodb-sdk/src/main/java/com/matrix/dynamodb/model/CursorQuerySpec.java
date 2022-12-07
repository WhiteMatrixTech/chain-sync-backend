package com.matrix.dynamodb.model;

import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

/**
 * @author reimia
 */
@Value
@Builder
public class CursorQuerySpec {

  /** equals {@link CursorQueryResult#getLastEvaluatedKey()}, can be null */
  Map<String, Object> exclusiveStartKey;

  /** cursor query must have limit */
  Integer limit;

  /** query order, default value is false, true is ascending, false is descending */
  boolean ascending;

  /**
   * exclusiveStartKey map to KeyAttribute array
   *
   * @return KeyAttribute array
   */
  public KeyAttribute[] exclusiveStartKeyAttributes() {
    return exclusiveStartKey.entrySet().stream()
        .map(entry -> new KeyAttribute(entry.getKey(), entry.getValue()))
        .toArray(KeyAttribute[]::new);
  }
}
