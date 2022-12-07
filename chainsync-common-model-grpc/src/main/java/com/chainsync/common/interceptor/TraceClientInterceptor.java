package com.chainsync.common.interceptor;

import com.chainsync.common.model.RequestHeaderConstants;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Context;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;

/**
 * @author luyuanheng
 */
@Log4j2
@GrpcGlobalClientInterceptor
public class TraceClientInterceptor implements ClientInterceptor {

  /**
   * Intercept {@link ClientCall} creation by the {@code next} {@link Channel}.
   *
   * <p>Many variations of interception are possible. Complex implementations may return a wrapper
   * around the result of {@code next.newCall()}, whereas a simpler implementation may just modify
   * the header metadata prior to returning the result of {@code next.newCall()}.
   *
   * <p>{@code next.newCall()} <strong>must not</strong> be called under a different {@link Context}
   * other than the current {@code Context}. The outcome of such usage is undefined and may cause
   * memory leak due to unbounded chain of {@code Context}s.
   *
   * @param method the remote method to be called.
   * @param callOptions the runtime options to be applied to this call.
   * @param next the channel which is being intercepted.
   * @return the call object for the remote operation, never {@code null}.
   */
  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
      MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
    return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
        next.newCall(method, callOptions)) {
      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        for (int i = 0; i < RequestHeaderConstants.tracingKeys.size(); i++) {
          String metadata = RequestHeaderConstants.contextKeys.get(i).get();
          if (metadata != null) {
            Metadata.Key<String> key = RequestHeaderConstants.tracingKeys.get(i);
            headers.put(key, metadata);
          }
        }

        super.start(
            new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
                responseListener) {
              @Override
              public void onHeaders(Metadata headers) {
                super.onHeaders(headers);
              }
            },
            headers);
      }
    };
  }
}
