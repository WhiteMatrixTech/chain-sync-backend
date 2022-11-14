package com.matrix.common.model;

import com.matrix.common.util.AddressUtil;

/** @author reimia */
public class EthereumAddress extends Address {

  public EthereumAddress(String address, ChainId chainId) {
    super(address, chainId);
  }

  @Override
  public String normalizeAddress(final String unstandardizedString) {
    return AddressUtil.normalizedEthereumAddress(unstandardizedString);
  }
}
