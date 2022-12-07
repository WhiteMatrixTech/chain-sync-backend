package com.matrix.common.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ErrorResponse extends Response<Exception> {

  private String code;

  /**
   * this constructor will return exception stacktrace, use it in a better way later
   */
  public ErrorResponse(final HttpStatus status, final Exception exception, final String code) {
    super(status, exception.getMessage(), exception);
    this.code = code;
  }

  public ErrorResponse(final HttpStatus status, final String message, final String code) {
    super(status, message, null);
    this.code = code;
  }
}
