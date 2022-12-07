package com.matrix.blockchain.model;

import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Ethereum's transaction payload
 *
 * @author ShenYang
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EthereumPayload implements BlockchainTransaction.Payload {

  /**
   * chain id number
   */
  private Long chainId;

  /**
   * gas limit
   */
  private BigInteger gasLimit;

  /**
   * to address
   */
  private String to;

  /**
   * transaction value
   */
  private BigInteger value;

  /**
   * transaction data before encode<br/>
   * <strong>Need to convert the paramList type to <code>com.matrix.blockchain.model.ContractParam</code>
   * while using</strong>
   */
  private EthereumTransactionRawData rawData;

  /**
   * transaction data - nonce
   */
  private BigInteger nonce;

  /**
   * max priority fee per gas
   */
  private BigInteger maxPriorityFeePerGas;

  /**
   * max fee per gas
   */
  private BigInteger maxFeePerGas;
}
