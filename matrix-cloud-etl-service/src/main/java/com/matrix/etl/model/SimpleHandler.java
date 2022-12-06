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
public class SimpleHandler {
  ChainType blockchain;
  String handlerName;
  HandlerType type;
  String kafkaTopic;
  String appName;
}
