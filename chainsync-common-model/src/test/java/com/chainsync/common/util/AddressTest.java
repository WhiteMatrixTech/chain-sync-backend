package com.chainsync.common.util;

import com.chainsync.common.model.Address;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author: ZhangKai
 */
public class AddressTest {

  @Test
  void fromFullAddress() {
    Address address = Address.fromFullAddress("mumbai_polygon-0x0000000000000000000000000000000000000aaa");
    Assertions.assertEquals("mumbai_polygon-0x0000000000000000000000000000000000000aaa", address.toString());
  }

}
