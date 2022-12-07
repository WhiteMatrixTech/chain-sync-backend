package com.chainsync.dynamodb.model;

import java.util.List;
import java.util.Map;
import lombok.Value;

/**
 * @author shuyizhang
 */
@Value
public class CursorQueryResult<T> {

  List<T> items;

  Map<String, Object> lastEvaluatedKey;
}
