package com.chainsync.common.model;

/**
 * @author shuyizhang
 */
public interface StringRepresentable<T> {
  String toStringRepresentation();

  T fromStringRepresentation(String stringRepresentation);
}
