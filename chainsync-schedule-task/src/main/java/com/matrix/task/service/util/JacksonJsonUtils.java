package com.matrix.task.service.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

/**
 * Jackson json utils.
 *
 * @author ShenYang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JacksonJsonUtils {

  /** Jackson object mapper. */
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * Read tree silently.
   *
   * @param content json string
   * @return JsonNode
   */
  @SneakyThrows
  public static JsonNode readTree(final String content) {
    return OBJECT_MAPPER.readTree(content);
  }
}
