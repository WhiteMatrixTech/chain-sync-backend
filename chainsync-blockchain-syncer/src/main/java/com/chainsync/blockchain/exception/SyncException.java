package com.chainsync.blockchain.exception;

import com.chainsync.common.exception.ErrorCodedException;
import com.chainsync.common.response.ResultCode;
import lombok.Getter;

/**
 * @author shuyizhang
 */
public class SyncException extends ErrorCodedException {

  @Getter private final Long blockNumber;

  SyncException(final Long blockNumber) {
    super(ResultCode.INTERNAL_SERVER_ERROR);
    this.blockNumber = blockNumber;
  }

  SyncException(final Long blockNumber, final String message) {
    super(ResultCode.INTERNAL_SERVER_ERROR, message);
    this.blockNumber = blockNumber;
  }

  SyncException(final Long blockNumber, final String message, final Throwable cause) {
    super(ResultCode.INTERNAL_SERVER_ERROR, message, cause);
    this.blockNumber = blockNumber;
  }
}
