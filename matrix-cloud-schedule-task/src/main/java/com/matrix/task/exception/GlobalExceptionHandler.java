package com.matrix.task.exception;

import com.matrix.common.response.ErrorResponse;
import com.matrix.common.response.ResultCode;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author luyuanheng
 */
@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(value = {IllegalArgumentException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleIllegalArgumentException(final IllegalArgumentException e) {
    log.warn("IllegalArgumentException: ", e);
    return new ErrorResponse(
        HttpStatus.BAD_REQUEST, e.getMessage(), ResultCode.ILLEGAL_ARGUMENT.getCode());
  }

  @ExceptionHandler(value = {MethodArgumentNotValidException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleMethodArgumentNotValidException(
      final MethodArgumentNotValidException e) {
    log.warn("MethodArgumentNotValidException: ", e);
    return new ErrorResponse(
        HttpStatus.BAD_REQUEST,
        e.getAllErrors().get(0).getDefaultMessage(),
        ResultCode.ILLEGAL_ARGUMENT.getCode());
  }

  @ExceptionHandler(value = {IllegalStateException.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleIllegalStateException(final IllegalStateException e) {
    log.error("IllegalStateException: ", e);
    return new ErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), ResultCode.ILLEGAL_STATE.getCode());
  }

  @ExceptionHandler(value = {Exception.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse defaultFallback(final Exception e) {
    log.error("default fallback: ", e);
    return new ErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        ResultCode.INTERNAL_SERVER_ERROR.getMessage(),
        ResultCode.INTERNAL_SERVER_ERROR.getCode());
  }
}
