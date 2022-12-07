package com.matrix.dynamodb.model;

import java.util.Map;
import lombok.Builder;
import lombok.Value;

/**
 * @author reimia
 */
@Value
@Builder
public class CursorPageQuerySpec {

  /** equals {@link CursorPageQueryResult#getPrevKey()}, can be null */
  Map<String, Object> prevKey;

  /** equals {@link CursorPageQueryResult#getNextKey()}, can be null */
  Map<String, Object> nextKey;

  /** cursor query must have limit */
  Integer limit;

  /** query order, default value is false, true is ascending, false is descending */
  boolean ascending;

  /** true means query next page, false means query prev page */
  boolean nextPage;
}
