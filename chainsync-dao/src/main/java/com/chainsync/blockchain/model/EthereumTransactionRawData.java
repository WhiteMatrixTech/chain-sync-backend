package com.chainsync.blockchain.model;

import com.chainsync.common.model.ContractParam;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This is a Ethereum transaction data before encode.
 *
 * @author ShenYang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EthereumTransactionRawData {

  /**
   * A part of transaction data before encoding. What information it holds depends on contract
   * behavior:
   * <ul>
   *   <li><strong>Deploy</strong> - it holds the contract template binary</li>
   *   <li><strong>Interaction</strong> - like mint, it holds the function name</li>
   * </ul>
   */
  private String part;

  /**
   * A type-value list.
   */
  private List<ContractParam> paramList;
}
