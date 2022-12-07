package com.chainsync.dynamodb.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

/**
 * Utilities for Base64 operation
 *
 * @author ShenYang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Base64Utils {

  /**
   * jackson mapper
   */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Map to base64 string
   *
   * @param map map
   * @return base64
   */
  @SneakyThrows
  public static String toBase64(final Map<String, Object> map) {
    return new String(Base64.getEncoder().encode(objectMapper.writeValueAsString(map).getBytes()));
  }

  /**
   * Base64 string to map
   *
   * @param base64 base64 string
   * @return map
   */
  @SneakyThrows
  @SuppressWarnings("unchecked")
  public static Map<String, Object> mapFromBase64(final String base64) {
    final String jsonStr = new String(Base64.getDecoder().decode(base64));
    return objectMapper.readValue(jsonStr, Map.class);
  }
}
