package com.matrix.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/** @author shuyizhang */
public enum AbiEntryType {

  /** event type */
  @JsonProperty("event")
  EVENT("event"),

  /** function type */
  @JsonProperty("function")
  FUNCTION("function"),

  /** function type */
  @JsonProperty("constructor")
  CONSTRUCTOR("constructor");

  @Getter
  private final String typeString;

  AbiEntryType(String typeString) {
    this.typeString = typeString;
  }

  @Override
  public String toString() {
    return this.typeString;
  }
}
