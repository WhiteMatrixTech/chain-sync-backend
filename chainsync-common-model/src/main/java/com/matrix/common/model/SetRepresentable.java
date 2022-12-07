package com.matrix.common.model;

import java.util.Set;

/**
 * @author shuyizhang
 */
public interface SetRepresentable<T> {
  String toSetRepresentation();

  T fromSetRepresentation(Set<?> setRepresentation);
}
