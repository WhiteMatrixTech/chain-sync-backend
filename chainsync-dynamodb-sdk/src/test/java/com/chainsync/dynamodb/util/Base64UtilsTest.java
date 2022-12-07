package com.chainsync.dynamodb.util;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Base64UtilsTest {

  @Test
  void testMap() {
    // encode
    final Map<String, Object> map = new HashMap<>();
    map.put("appId", "FlowMarketplace");
    map.put("userId", "0x8734440a1591a0b5_flow");
    map.put("username", "asdfasdfsadfsadfasdf");
    map.put("intVal", 123);
    final String base64 = Base64Utils.toBase64(map);
    Assertions.assertNotNull(base64);
    Assertions.assertFalse(base64.isBlank());

    // decode
    final Map<String, Object> decodedMap = Base64Utils.mapFromBase64(base64);
    Assertions.assertNotNull(base64);
    Assertions.assertEquals(map, decodedMap);
  }
}