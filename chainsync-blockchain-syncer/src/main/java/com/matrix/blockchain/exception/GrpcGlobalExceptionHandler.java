package com.matrix.blockchain.exception;

import com.matrix.common.handler.CommonGrpcGlobalExceptionHandler;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.advice.GrpcAdvice;

@Log4j2
@GrpcAdvice
public class GrpcGlobalExceptionHandler extends CommonGrpcGlobalExceptionHandler {}
