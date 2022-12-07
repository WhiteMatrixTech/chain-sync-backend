package com.matrix.common.util;

import com.google.rpc.Code;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author reimia
 */
public class HttpGrpcCodeMapperUtil {
  private HttpGrpcCodeMapperUtil() {}

  private static final Map<Integer, Code> map = new HashMap<>();

  static {
    map.put(200, Code.OK);
    map.put(400, Code.INVALID_ARGUMENT);
    map.put(401, Code.UNAUTHENTICATED);
    map.put(403, Code.PERMISSION_DENIED);
    map.put(404, Code.NOT_FOUND);
    map.put(409, Code.ALREADY_EXISTS);
    map.put(412, Code.FAILED_PRECONDITION);
    map.put(499, Code.CANCELLED);
    map.put(500, Code.INTERNAL);
  }

  public static int convertToGrpc(final int httpCode) {
    return map.get(httpCode).getNumber();
  }

  public static int convertToHttp(final int code) {
    return map.entrySet().stream()
        .filter(kvEntry -> Objects.equals(kvEntry.getValue(), Code.forNumber(code)))
        .map(Entry::getKey)
        .collect(Collectors.toList())
        .get(0);
  }
}
