package com.matrix.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author reimia
 */
class AddressUtilTest {

  @Test
  void normalizedFlowAddress() {
    final String s1 = AddressUtil.normalizedFlowAddress("0xbbaa111222333444");
    final String s2 = AddressUtil.normalizedFlowAddress("bbaa111222333444");
    final String s3 = AddressUtil.normalizedFlowAddress("0x00aa111222333444");
    final String s4 = AddressUtil.normalizedFlowAddress("aaa111222333444");
    final String s5 = AddressUtil.normalizedFlowAddress("aa111222333444");
    final String s6 = AddressUtil.normalizedFlowAddress("0xaa111222333444");
    Assertions.assertEquals("0xbbaa111222333444", s1);
    Assertions.assertEquals("0xbbaa111222333444", s2);
    Assertions.assertEquals("0x00aa111222333444", s3);
    Assertions.assertEquals("0x0aaa111222333444", s4);
    Assertions.assertEquals("0x00aa111222333444", s5);
    Assertions.assertEquals("0x00aa111222333444", s6);
  }
}
