package com.chainsync.common.model;

import com.chainsync.common.util.AddressUtil;

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
