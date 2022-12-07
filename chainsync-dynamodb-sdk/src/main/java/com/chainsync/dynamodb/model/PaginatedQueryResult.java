package com.chainsync.dynamodb.model;

import java.util.List;
import lombok.Value;

/**
 * @author shuyizhang
 */
@Value
public class PaginatedQueryResult<T, P> {

  List<T> items;
  P paginator;
}
