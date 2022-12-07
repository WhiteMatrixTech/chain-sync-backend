package com.chainsync.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * @author reimia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

  @NonNull private HttpStatus status;

  private static final String SUCCESS_MESSAGE = "request succeeded.";

  private String message;

  private T data;

  public static <T> Response<T> success(final T data) {
    return new Response<>(HttpStatus.OK, SUCCESS_MESSAGE, data);
  }

  public static <T> Response<T> success() {
    return success(null);
  }

  public static boolean isSuccess(@NonNull final Response<?> response) {
    return response.getStatus().is2xxSuccessful();
  }
}
