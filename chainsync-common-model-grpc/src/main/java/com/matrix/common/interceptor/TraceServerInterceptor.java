package com.matrix.common.interceptor;

import static com.matrix.common.model.RequestHeaderConstants.contextKeys;
import static com.matrix.common.model.RequestHeaderConstants.tracingKeys;

import io.grpc.ClientCall;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

/**
 * @author luyuanheng
 */
@Log4j2
@GrpcGlobalServerInterceptor
public class TraceServerInterceptor implements ServerInterceptor {

  /**
   * Intercept {@link ServerCall} dispatch by the {@code next} {@link ServerCallHandler}. General
   * semantics of {@link ServerCallHandler#startCall} apply and the returned {@link Listener} must
   * not be {@code null}.
   *
   * <p>If the implementation throws an exception, {@code call} will be closed with an error.
   * Implementations must not throw an exception if they started processing that may use {@code
   * call} on another thread.
   *
   * @param call object to receive response messages
   * @param headers which can contain extra call metadata from {@link ClientCall#start}, e.g.
   *     authentication credentials.
   * @param next next processor in the interceptor chain
   * @return listener for processing incoming messages for {@code call}, never {@code null}.
   */
  @Override
  public <ReqT, RespT> Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    Context current = Context.current();
    for (int i = 0; i < tracingKeys.size(); i++) {
      Metadata.Key<String> tracingKey = tracingKeys.get(i);
      String metadata = headers.get(tracingKey);
      if (metadata != null) {
        Context.Key<String> key = contextKeys.get(i);
        current = current.withValue(key, metadata);
      }
    }

    return Contexts.interceptCall(current, call, headers, next);
  }
}
