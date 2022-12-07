package com.chainsync.eventhandler.model;

import java.util.Map;
import lombok.Builder;
import lombok.Value;

/**
 * @author reimia
 */
@Value
@Builder
public class PhantaciNotify {
  String address;
  Long blockNumber;
  Long blockTimestamp;
  String eventName;
  Map<String, Object> payload;
}
