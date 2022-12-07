package com.matrix.common.model;

import java.util.Map;

/**
 * @author shuyizhang
 */
public interface MapRepresentable<T> {
  Map<String, ?> toMapRepresentation();

  T fromMapRepresentation(Map<String, ?> mapRepresentation);
}
