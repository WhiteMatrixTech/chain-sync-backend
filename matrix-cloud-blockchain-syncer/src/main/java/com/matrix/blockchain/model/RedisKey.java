package com.matrix.blockchain.model;

/**
 * @author luyuanheng
 */
public class RedisKey {

  /**
   * prefix:chainId:blockNumber
   */
  public static final String BLOCK = "matrix_market:block:%s:%s";
}
