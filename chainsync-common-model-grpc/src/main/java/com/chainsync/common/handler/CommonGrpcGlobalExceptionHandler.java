package com.chainsync.common.handler;

import com.chainsync.common.Resources.ErrorDetail;
import com.chainsync.common.exception.ErrorCodedException;
import com.chainsync.common.util.HttpGrpcCodeMapperUtil;
import com.google.protobuf.Any;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.springframework.beans.factory.annotation.Value;

@Log4j2
@GrpcAdvice
public class CommonGrpcGlobalExceptionHandler {

  @Value("${spring.application.name}")
  private String applicationName;

  @GrpcExceptionHandler(ErrorCodedException.class)
  public StatusRuntimeException handleErrorCodedException(final ErrorCodedException e) {
    log.error("[GrpcGlobalExceptionHandler]handleErrorCodedException", e);
    final ErrorDetail errorDetail =
        ErrorDetail.newBuilder()
            .setCode(e.getResultCodeInfo().getCode())
            .setMessage(e.getResultCodeInfo().getMessage())
            .build();
    return StatusProto.toStatusRuntimeException(
        com.google.rpc.Status.newBuilder()
            .setCode(
                HttpGrpcCodeMapperUtil.convertToGrpc(e.getResultCodeInfo().getHttpStatus().value()))
            .setMessage(e.getMessage())
            .addDetails(Any.pack(errorDetail))
            .build());
  }

  @GrpcExceptionHandler(IllegalArgumentException.class)
  public StatusRuntimeException handleIllegalArgumentException(final IllegalArgumentException e) {
    log.error("[GrpcGlobalExceptionHandler]handleIllegalArgumentException", e);
    return Status.INVALID_ARGUMENT
        .withDescription(e.getMessage())
        .asRuntimeException(defaultErrorMetadata());
  }

  @GrpcExceptionHandler(Exception.class)
  public StatusRuntimeException handleDefaultException(final Exception e) {
    log.error("[GrpcGlobalExceptionHandler]handleDefaultException", e);
    return Status.INTERNAL
        .withDescription(e.getMessage())
        .asRuntimeException(defaultErrorMetadata());
  }

  @GrpcExceptionHandler(StatusRuntimeException.class)
  public StatusRuntimeException handleStatusRuntimeException(final StatusRuntimeException e) {
    log.error("[GrpcGlobalExceptionHandler]handleStatusRuntimeException", e);
    return e;
  }

  private Metadata defaultErrorMetadata() {
    final Metadata trailers = new Metadata();
    trailers.put(Key.of("domain", Metadata.ASCII_STRING_MARSHALLER), applicationName);
    return trailers;
  }
}
