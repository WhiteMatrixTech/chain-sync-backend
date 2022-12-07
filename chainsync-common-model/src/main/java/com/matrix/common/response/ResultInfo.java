package com.matrix.common.response;

import org.springframework.http.HttpStatus;

/**
 * @author reimia
 */
public interface ResultInfo {

  /** return status code */
  String getCode();

  /** return message */
  String getMessage();

  HttpStatus getHttpStatus();
}
