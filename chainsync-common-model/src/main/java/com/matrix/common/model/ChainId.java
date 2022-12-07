package com.matrix.common.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * @author reimia
 */
@Value
@Jacksonized
@Builder
public class ChainId {

  private static final String SEPARATOR = "_";

  private static final int LENGTH = 3;

  ChainType chainType;

  ChainName chainName;

  Long chainIdNumber;

  /**
   * convert to string
   *
   * @return ChainId string
   */
  @Override
  public String toString() {
    return chainName + SEPARATOR + chainType;
  }

  /**
   * convert a ChainId string to object.
   *
   * @param str ChainId string
   * @return ChainId object
   */
  public static ChainId fromString(String str) {
    String[] sArr = str.split(SEPARATOR);
    return ChainId.builder()
        .chainName(ChainName.valueOf(sArr[0]))
        .chainType(ChainType.valueOf(sArr[1]))
        .build();
  }
}
