package com.matrix.blockchain.model;

import com.matrix.eventhandler.model.BlockchainEventLogDTO;
import com.matrix.marketplace.blockchain.util.PaddingUtil;

/**
 * @author luyuanheng
 */
public abstract class BlockEvent {

  public static final long BATCH_SIZE = 10000;

  public static final int BLOCK_NUMBER_LEN = 16;

  public static final int TX_INDEX_LEN = 8;

  public static final int LOG_INDEX_LEN = 8;

  public abstract BlockchainEventLogDTO convertToAvro(BlockRange blockRange);

  /**
   * key = blockNumber:logIndex
   *
   * @return key
   */
  public abstract String getKey();

  /**
   * contractAddress as kafka key, ensure the sequence of events in the contract
   *
   * @return key
   */
  public abstract String getKafkaKey();

  /**
   * key = {0} * n + blockNumber + {0} * m + logIndex
   *
   * @param blockNumber
   * @param logIndex
   * @return eventKey
   */
  public String resolveEventKey(final Long blockNumber, final Long logIndex) {
    return PaddingUtil.paddingAndConnect(
        blockNumber, BLOCK_NUMBER_LEN, logIndex, LOG_INDEX_LEN, PaddingUtil.ZERO);
  }
}
