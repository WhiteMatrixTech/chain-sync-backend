package com.matrix.eventhandler.controller;

import com.google.protobuf.Empty;
import com.matrix.eventhandler.event.BlockchainEventHandlerManager;
import com.matrix.eventhandler.model.BlockchainEventHandler;
import com.matrix.eventhandler.model.BlockchainEventHandlerServiceGrpc.BlockchainEventHandlerServiceImplBase;
import com.matrix.eventhandler.model.BlockchainEventHandlers;
import io.grpc.stub.StreamObserver;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * @author reimia
 */
@Log4j2
@GrpcService
public class BlockchainEventHandlerController extends BlockchainEventHandlerServiceImplBase {

  @Resource BlockchainEventHandlerManager blockchainEventHandlerManager;

  @Override
  public void getHandlers(
      final Empty request, final StreamObserver<BlockchainEventHandlers> responseObserver) {
    final BlockchainEventHandlers.Builder builder = BlockchainEventHandlers.newBuilder();
    blockchainEventHandlerManager
        .getBlockchainEventHandlers()
        .forEach(
            blockchainEventHandler ->
                builder.addHandlers(
                    BlockchainEventHandler.newBuilder()
                        .setGroup(blockchainEventHandler.getGroup())
                        .setName(blockchainEventHandler.getClass().getSimpleName())
                        .build()));
    responseObserver.onNext(builder.build());
    responseObserver.onCompleted();
  }
}
