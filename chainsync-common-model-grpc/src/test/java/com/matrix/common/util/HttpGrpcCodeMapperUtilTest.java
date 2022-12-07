package com.matrix.common.util;

import com.google.rpc.Code;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author reimia
 */
class HttpGrpcCodeMapperUtilTest {

  @Test
  void convertToGrpc() {
    int i = HttpGrpcCodeMapperUtil.convertToGrpc(400);
    Assertions.assertEquals(Code.INVALID_ARGUMENT_VALUE, i);
  }

  @Test
  void convertToHttp() {
    int i = HttpGrpcCodeMapperUtil.convertToHttp(Code.INTERNAL_VALUE);
    Assertions.assertEquals(500, i);
  }
}
