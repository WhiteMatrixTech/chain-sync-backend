package com.chainsync.common.model;

import com.chainsync.common.util.HashUtils;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * Ethereum's transaction hash
 *
 * @author ShenYang
 */
@JsonSerialize(using = ToStringSerializer.class)
public class EthereumTransactionHash extends TransactionHash {

  public EthereumTransactionHash(String hash, ChainName chainName) {
    super(hash, ChainType.ethereum, chainName);
  }

  @Override
  protected String normalizeHash(String hash) {
    return HashUtils.toLowercaseHex(hash);
  }
}
