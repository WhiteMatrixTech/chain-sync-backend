package com.matrix.common.model;

import com.matrix.common.util.AddressUtil;

/**
 * @author reimia
 */
public class FlowAddress extends Address {

  public FlowAddress(final String address, final ChainId chainId) {
    super(address, chainId);
  }

  @Override
  public String normalizeAddress(final String unnormalizedAddress) {
    return AddressUtil.normalizedFlowAddress(unnormalizedAddress);
  }
}
