package com.matrix.common.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.matrix.common.util.HashUtils;

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
