package com.matrix.etl.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * @author richard
 */
@Builder
@Jacksonized
@Value
public class SimpleApp {
  ChainType blockchain;
  String appName;
  List<String> handlers;
}
