package com.chainsync.common.model;

import lombok.Getter;

/**
 * transaction hash with chain type
 *
 * @author ShenYang
 */
@Getter
public abstract class TransactionHash {

  private final String hash;

  private final ChainType chainType;

  private final ChainName chainName;

  protected TransactionHash(String hash, ChainType chainType, ChainName chainName) {
    this.hash = normalizeHash(hash);
    this.chainType = chainType;
    this.chainName = chainName;
  }

  protected abstract String normalizeHash(String hash);

  /**
   * Create a transaction hash from a canonical hash
   *
   * @param canonicalHash e.g. 0x0000000000000000000000000000000000000456_ethereum_mainnet
   * @return TransactionHash
   */
  public static TransactionHash fromCanonicalHash(String canonicalHash) {
    String[] arr = canonicalHash.split("_");
    ChainType chainType = ChainType.valueOf(arr[1]);
    switch (chainType) {
      case ethereum:
        return new EthereumTransactionHash(arr[0], ChainName.valueOf(arr[2]));
      case flow:
        throw new UnsupportedOperationException("Chain[flow] still not supported yet.");
      default:
        throw new IllegalArgumentException(
            String.format("Unsupported transaction hash chain[%s]!", chainType)
        );
    }
  }

  @Override
  public String toString() {
    return hash + "_" + chainType + "_" + chainName;
  }
}
