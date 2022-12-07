package com.chainsync.dynamodb.model;

import lombok.Builder;
import lombok.Value;

/**
 * @author shuyizhang
 */
@Value
@Builder
public class PaginatedQuerySpec<P> {

  P from;
  P paginator;
  Integer limit;
  P to;
}
