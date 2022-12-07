package com.matrix.dynamodb.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * @author shuyizhang
 */
public class SerdeUtil {
  /** plain object mapper used to do normal serde for DynamoDB mapping */
  public static final ObjectMapper DEFAULT_OBJECT_MAPPER =
      new ObjectMapper().registerModule(new JavaTimeModule());
}
