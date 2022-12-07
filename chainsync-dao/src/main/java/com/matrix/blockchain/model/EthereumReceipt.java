package com.matrix.blockchain.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The receipt of an Ethereum transaction
 *
 * @author ShenYang
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EthereumReceipt implements BlockchainTransaction.Receipt {

  /**
   * transaction hash
   */
  private String transactionHash;

  /**
   * transaction index
   */
  private String transactionIndex;

  /**
   * block hash
   */
  private String blockHash;

  /**
   * block num
   */
  private String blockNumber;

  /**
   * Accumulation of gas used
   */
  private String cumulativeGasUsed;

  /**
   * gas used
   */
  private String gasUsed;

  /**
   * contract address
   */
  private String contractAddress;

  /**
   * root
   */
  private String root;

  /**
   * status
   */
  private String status;

  /**
   * from address
   */
  private String from;

  /**
   * to address
   */
  private String to;

  /**
   * a <code>org.web3j.protocol.core.methods.response.Log</code> list
   */
  private List<?> logs;

  /**
   * logs bloom?
   */
  private String logsBloom;

  /**
   * reason of revert
   */
  private String revertReason;

  /**
   * type
   */
  private String type;

  /**
   * effective gas price
   */
  private String effectiveGasPrice;
}
