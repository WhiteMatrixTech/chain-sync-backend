package com.matrix.common.model;

import com.matrix.common.util.AddressUtil;

/**
 * @author reimia
 */
public class PolygonAddress extends Address {

  public PolygonAddress(String address, ChainId chainId) {
    super(address, chainId);
  }

  @Override
  public String normalizeAddress(final String unstandardizedString) {
    return AddressUtil.normalizedEthereumAddress(unstandardizedString);
  }
}
