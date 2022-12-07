package com.matrix.etl.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * @author richard
 */
@Builder
@Jacksonized
@Value
public class SimpleBlock {
  Long blockNumber;
  Integer size;
  String timestamp;
  Long gasUsed;
  Integer transactionCount;
}
