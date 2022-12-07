package com.chainsync.common.util;

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

/**
 * @author shuyizhang
 */
public class AddressUtil {

  private static final String HEX_PREFIX = "0x";
  private static final int FLOW_ADDRESS_LENGTH = 18;
  private static final String FLOW_CONTRACT_SEPARATOR = "\\.";
  private static final int ETH_RADIX = 16;
  private static final int ETH_ADDRESS_LENGTH = 40;

  public static String normalizedEthereumAddress(final String address) {
    final String lowercaseAddress =
        StringUtils.leftPad(Numeric.cleanHexPrefix(address).toLowerCase(), ETH_ADDRESS_LENGTH, "0");
    final String addressHash = Numeric.cleanHexPrefix(Hash.sha3String(lowercaseAddress));

    final StringBuilder result = new StringBuilder(lowercaseAddress.length() + 2);

    result.append("0x");

    for (int i = 0; i < lowercaseAddress.length(); i++) {
      if (Integer.parseInt(String.valueOf(addressHash.charAt(i)), ETH_RADIX) >= 8) {
        result.append(String.valueOf(lowercaseAddress.charAt(i)).toUpperCase());
      } else {
        result.append(lowercaseAddress.charAt(i));
      }
    }

    return result.toString();
  }

  public static String normalizedFlowAddress(final String flowAddress) {
    if (!flowAddress.contains(".")) {
      return normalizedFlowAccountAddress(flowAddress);
    }
    return normalizedFlowContractAddress(flowAddress);
  }

  public static String normalizedFlowAccountAddress(final String flowAddress) {
    if (flowAddress.startsWith(HEX_PREFIX) && flowAddress.length() == FLOW_ADDRESS_LENGTH) {
      return flowAddress;
    }
    String trueAddress = flowAddress;
    if (flowAddress.startsWith(HEX_PREFIX)) {
      trueAddress = flowAddress.substring(2);
    }
    while (trueAddress.length() < 16) {
      trueAddress = "0".concat(trueAddress);
    }
    return HEX_PREFIX + trueAddress;
  }

  public static String normalizedFlowContractAddress(final String flowAddress) {
    if (flowAddress.split(FLOW_CONTRACT_SEPARATOR).length > 3) {
      final int i = StringUtils.ordinalIndexOf(flowAddress, ".", 3);
      if (i > 0) {
        return flowAddress.substring(0, i);
      }
    }
    return flowAddress;
  }
}
