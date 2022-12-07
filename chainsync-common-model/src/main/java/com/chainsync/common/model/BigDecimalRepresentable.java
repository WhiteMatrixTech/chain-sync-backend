package com.chainsync.common.model;

import java.math.BigDecimal;

/**
 * @author shuyizhang
 */
public interface BigDecimalRepresentable<T> {
  String toBigDecimalRepresentation();

  T fromBigDecimalRepresentation(BigDecimal bigDecimalRepresentation);
}
