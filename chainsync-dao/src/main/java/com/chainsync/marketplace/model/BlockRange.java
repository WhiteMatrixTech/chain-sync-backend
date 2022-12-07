package com.chainsync.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author richard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockRange {
  String chainType;
  long from;
  long to;
}
