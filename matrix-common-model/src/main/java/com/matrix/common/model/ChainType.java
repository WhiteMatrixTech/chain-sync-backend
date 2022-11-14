package com.matrix.common.model;

/**
 * @author yangjian
 * @date 2021/12/30 PM 6:34
 */
public enum ChainType {
  /**
   * Ethereum blockchain, due to historical issue we have to use lower case for
   * backward-compatibility TODO make it cap in the future
   */
  ethereum,
  polygon,
  flow,
  aptos
}
