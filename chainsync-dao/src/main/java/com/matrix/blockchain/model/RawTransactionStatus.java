package com.matrix.blockchain.model;

/**
 * Transaction status enum
 *
 * @author ShenYang
 */
public enum RawTransactionStatus {

  /**
   * Transaction in pending status.
   */
  PENDING_ETH,

  /**
   * Transaction succeeded in result.
   */
  SUCCEEDED_ETH,

  /**
   * Transaction encountered an error(errors).
   */
  ERROR_ETH,

  /**
   * Unknown status of transaction.
   */
  UNKNOWN_ETH,

  /**
   * DROPPED
   */
  DROPPED_ETH
}
