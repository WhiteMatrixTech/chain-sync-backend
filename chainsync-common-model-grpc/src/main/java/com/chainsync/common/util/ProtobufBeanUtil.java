package com.chainsync.common.util;

import com.google.gson.Gson;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import lombok.SneakyThrows;

/**
 * @author reimia
 */
public class ProtobufBeanUtil {

  private static final Gson gson = new Gson();

  private ProtobufBeanUtil() {}

  @SneakyThrows
  public static <PojoType> PojoType toJavaBean(
      final Class<PojoType> destPojoClass, final Message sourceMessage) {
    final String json = JsonFormat.printer().print(sourceMessage);
    return gson.fromJson(json, destPojoClass);
  }

  @SneakyThrows
  public static void toProtoBean(final Message.Builder destBuilder, final Object sourcePojoBean) {
    final String json = gson.toJson(sourcePojoBean);
    JsonFormat.parser().merge(json, destBuilder);
  }
}
