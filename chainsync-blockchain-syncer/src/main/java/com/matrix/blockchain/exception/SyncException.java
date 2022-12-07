package com.matrix.blockchain.exception;

import com.matrix.common.exception.ErrorCodedException;
import com.matrix.common.response.ResultCode;
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
