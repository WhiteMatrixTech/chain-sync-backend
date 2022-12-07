package com.chainsync.marketplace.util;

/**
 * @author luyuanheng
 */
public class PaddingUtil {

  public static final String ZERO = "0";

  public static String paddingAndConnect(
      final Object a, final int aLen, final Object b, final int bLen, final String paddingStr) {
    final StringBuilder result = new StringBuilder();

    final int aOriginLen = a.toString().length();
    if (aOriginLen < aLen) {
      result.append(paddingStr.repeat(aLen - aOriginLen));
    }

    result.append(a);

    final int bOriginLen = b.toString().length();
    if (bOriginLen < bLen) {
      result.append(paddingStr.repeat(bLen - bOriginLen));
    }

    result.append(b);

    return result.toString();
  }
}
