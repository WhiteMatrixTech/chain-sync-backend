package com.chainsync.common.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChainTypeTest {

  @Test
  public void testConvertRoundTrip() {
    final String enumString = "ethereum";
    final ChainType chainType = ChainType.valueOf(enumString);
    Assertions.assertEquals(ChainType.ethereum, chainType);
    Assertions.assertEquals(enumString, chainType.toString());
  }
}
