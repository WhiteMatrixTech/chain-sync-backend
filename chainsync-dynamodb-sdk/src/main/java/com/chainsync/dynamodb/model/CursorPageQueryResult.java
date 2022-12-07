package com.chainsync.dynamodb.model;

import java.util.List;
import java.util.Map;
import lombok.Value;

/**
 * @author reimia
 */
@Value
public class CursorPageQueryResult<T> {
  List<T> items;

  Map<String, Object> prevKey;

  Map<String, Object> nextKey;
}
