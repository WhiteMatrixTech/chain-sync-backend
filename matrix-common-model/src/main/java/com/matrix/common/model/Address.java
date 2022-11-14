package com.matrix.common.model;

import java.util.Objects;
import lombok.Getter;

/**
 * @author reimia
 */
@Getter
public abstract class Address {

  private static final String SEPARATOR = "-";

  private final ChainId chainId;

  private final String normalizedAddress;

  private final String canonicalAddress;

  public Address(final String address, final ChainId chainId) {
    this.normalizedAddress = normalizeAddress(address);
    this.chainId = chainId;
    this.canonicalAddress = chainId.toString().concat(SEPARATOR).concat(this.normalizedAddress);
  }

  /**
   * Convert an unstandardized normal string to a normalized address, this is chain specific logic
   *
   * @param unnormalizedAddress
   * @return a normalizedAddress
   */
  public abstract String normalizeAddress(final String unnormalizedAddress);

  public static Address fromFullAddress(final String canonicalAddress) {
    final String chainIdStr = canonicalAddress.substring(0, canonicalAddress.indexOf(SEPARATOR));
    final String address = canonicalAddress.substring(canonicalAddress.indexOf(SEPARATOR) + 1);
    final ChainId chainId = ChainId.fromString(chainIdStr);

    return fromAddressAndChainId(address, chainId);
  }

  /**
   * @param address raw address, such as 0x12345678
   */
  public static Address fromAddressAndChainId(final String address, final ChainId chainId) {
    switch (chainId.getChainType()) {
      case ethereum:
        return new EthereumAddress(address, chainId);
      case polygon:
        return new PolygonAddress(address, chainId);
      case flow:
        return new FlowAddress(address, chainId);
      default:
        throw new IllegalArgumentException("unsupported chainType: " + chainId.getChainType());
    }
  }

  @Override
  public String toString() {
    return canonicalAddress;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    return Objects.equals(canonicalAddress, ((Address) o).getCanonicalAddress());
  }

  @Override
  public int hashCode() {
    return Objects.hash(canonicalAddress);
  }
}
