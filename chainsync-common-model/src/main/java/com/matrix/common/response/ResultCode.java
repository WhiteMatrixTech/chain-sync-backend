package com.matrix.common.response;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * @author reimia
 */
@AllArgsConstructor
@NoArgsConstructor
public enum ResultCode implements ResultInfo {
  /** success */
  SUCCESS("00000", "Request succeeded", HttpStatus.OK),

  /** generic errors */
  INTERNAL_SERVER_ERROR(
      "5XXG1",
      "Unexpected error occured, please try again in a few minutes.",
      HttpStatus.INTERNAL_SERVER_ERROR),
  ILLEGAL_STATE("5XXG2", "Unexpected error occured", HttpStatus.INTERNAL_SERVER_ERROR),

  ILLEGAL_ARGUMENT("4XXG1", "Bad or illegal user input", HttpStatus.BAD_REQUEST),
  NOT_FOUND("4XXG2", "Requested resource not found", HttpStatus.NOT_FOUND),
  ALREADY_EXISTS("4XXG3", "Resource already existed", HttpStatus.CONFLICT),
  UNAUTHORIZED("4XXG4", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED),
  FORBIDDEN("4XXG5", "FORBIDDEN", HttpStatus.FORBIDDEN);

  private static final Map<String, ResultCode> CODE_TO_VALUE_MAP = getCodeToValueMap();

  private String code;

  private String message;

  private HttpStatus httpStatus;

  public static ResultCode getEnumValueFromCode(final String code) {
    Preconditions.checkArgument(CODE_TO_VALUE_MAP.containsKey(code), "Invalid code %s", code);
    return CODE_TO_VALUE_MAP.get(code);
  }

  private static Map<String, ResultCode> getCodeToValueMap() {
    Map<String, ResultCode> codeToValueMap = new HashMap<>();
    for (ResultCode value : values()) {
      codeToValueMap.put(value.getCode(), value);
    }
    return codeToValueMap;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  @Override
  public String toString() {
    return "ResultCode{" + "code='" + code + '\'' + ", message='" + message + '\'' + '}';
  }
}
