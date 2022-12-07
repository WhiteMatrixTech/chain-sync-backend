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
public class SimpleTransaction {
  Long blockNumber;
  String transactionHash;
  String timestamp;
  String from;
  String to;
  String value;
}
