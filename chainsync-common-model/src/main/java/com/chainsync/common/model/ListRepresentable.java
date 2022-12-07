package com.chainsync.common.model;

import java.util.List;

/**
 * @author shuyizhang
 */
public interface ListRepresentable<T> {
  String toListRepresentation();

  T fromListRepresentation(List<?> listRepresentation);
}
