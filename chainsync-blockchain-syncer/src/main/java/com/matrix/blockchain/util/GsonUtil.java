package com.matrix.blockchain.util;

import com.google.gson.Gson;

/**
 * @author luyuanheng
 */
public class GsonUtil {

  private static final Gson GSON = new Gson();

  public static String toJson(Object object) {
    return GSON.toJson(object);
  }
}
