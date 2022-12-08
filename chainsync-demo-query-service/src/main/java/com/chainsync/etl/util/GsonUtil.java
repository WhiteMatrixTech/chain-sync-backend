package com.chainsync.etl.util;

import com.google.gson.Gson;

/**
 * @author luyuanheng
 */
public class GsonUtil {

  public static final Gson GSON = new Gson();

  public static String toJson(final Object object) {
    return GSON.toJson(object);
  }
}
