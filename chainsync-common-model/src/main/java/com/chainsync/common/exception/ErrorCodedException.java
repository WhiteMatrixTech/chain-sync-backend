package com.chainsync.common.exception;

import com.chainsync.common.response.ResultInfo;
import com.chainsync.common.response.ErrorResponse;
import lombok.Getter;

/**
 * @author reimia
 */
@Getter
public class ErrorCodedException extends RuntimeException {

  private final ResultInfo resultCodeInfo;

  public ErrorCodedException(final ResultInfo errorInfo) {
    super(errorInfo.getMessage());
    this.resultCodeInfo = errorInfo;
  }

  public ErrorCodedException(final ResultInfo errorType, final String message) {
    super(message);
    this.resultCodeInfo = errorType;
  }

  public ErrorCodedException(
      final ResultInfo errorType, final String message, final Throwable cause) {
    super(message, cause);
    this.resultCodeInfo = errorType;
  }

  public ErrorResponse toErrorResponse() {
    return new ErrorResponse(
        getResultCodeInfo().getHttpStatus(), this.getMessage(), getResultCodeInfo().getCode());
  }
}
