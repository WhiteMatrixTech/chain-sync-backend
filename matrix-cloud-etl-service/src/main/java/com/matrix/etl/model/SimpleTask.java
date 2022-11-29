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
public class SimpleTask {
  String blockchain;
  String taskName;
  Long taskId;
  Long createTime;
  String status;
}
