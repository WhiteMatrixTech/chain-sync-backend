package com.chainsync.common.util;

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

/**
 * Hash utils
 *
 * @author ShenYang
 */
public class HashUtils {

  /**
   * to lowercase hex string
   *
   * @param hexHash hex hash
   * @return lowercase hex string
   */
  public static String toLowercaseHex(String hexHash) {
    return ("0x" + Numeric.cleanHexPrefix(hexHash)).toLowerCase();
  }
}
