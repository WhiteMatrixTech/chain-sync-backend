package com.matrix.blockchain.controller;

import com.google.protobuf.Int64Value;
import com.matrix.blockchain.model.BlockChainSyncServiceGrpc;
import com.matrix.blockchain.model.BlockRange;
import com.matrix.blockchain.model.GetOffsetRequest;
import com.matrix.blockchain.model.GetTransactionEventsRequest;
import com.matrix.blockchain.model.GetTransactionEventsResponse;
import com.matrix.blockchain.model.RetryRequest;
import com.matrix.blockchain.model.SyncResponse;
import com.matrix.blockchain.model.SyncResult;
import com.matrix.blockchain.model.SyncStep;
import com.matrix.blockchain.service.BlockService;
import com.matrix.blockchain.service.SyncRequestService;
import com.matrix.blockchain.service.SyncerService;
import io.grpc.stub.StreamObserver;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;

/**
 * @author luyuanheng
 */
@Log4j2
@GrpcService
@Component
public class BlockChainSyncerController
    extends BlockChainSyncServiceGrpc.BlockChainSyncServiceImplBase {

  @Resource SyncerService syncerService;

  @Resource SyncRequestService syncRequestService;

  @Resource BlockService blockService;

  /**
   * @param request
   * @param responseObserver
   */
  @Override
  public void runSyncTask(
      final SyncStep request, final StreamObserver<SyncResult> responseObserver) {
    responseObserver.onNext(syncerService.runSyncTask(request));
    responseObserver.onCompleted();
  }

  /**
   * @param request
   * @param responseObserver
   */
  @Override
  public void rangeSyncTask(
      final BlockRange request, final StreamObserver<SyncResult> responseObserver) {
    responseObserver.onNext(syncerService.rangeSyncTask(request));
    responseObserver.onCompleted();
  }

  /**
   * @param request
   * @param responseObserver
   */
  @Override
  public void retrySyncTask(
      final RetryRequest request, final StreamObserver<SyncResult> responseObserver) {
    responseObserver.onNext(syncerService.retrySyncTask(request));
    responseObserver.onCompleted();
  }

  /**
   * @param request
   * @param responseObserver
   */
  @Override
  public void runSyncRequest(
      final SyncStep request, final StreamObserver<SyncResponse> responseObserver) {
    responseObserver.onNext(syncRequestService.runSyncTask(request));
    responseObserver.onCompleted();
  }

  /**
   * @param request
   * @param responseObserver
   */
  @Override
  public void rangeSyncRequest(
      final BlockRange request, final StreamObserver<SyncResponse> responseObserver) {
    responseObserver.onNext(syncRequestService.rangeSyncTask(request));
    responseObserver.onCompleted();
  }

  /**
   * @param request
   * @param responseObserver
   */
  @Override
  public void retrySyncRequest(
      final RetryRequest request, final StreamObserver<SyncResponse> responseObserver) {
    responseObserver.onNext(syncRequestService.retrySyncTask(request));
    responseObserver.onCompleted();
  }

  /**
   * @param request
   * @param responseObserver
   */
  @Override
  public void getTransactionEvents(
      final GetTransactionEventsRequest request,
      final StreamObserver<GetTransactionEventsResponse> responseObserver) {
    responseObserver.onNext(blockService.getTransactionEvents(request));
    responseObserver.onCompleted();
  }

  /**
   * @param request
   * @param responseObserver
   */
  @Override
  public void getOffset(
      final GetOffsetRequest request, final StreamObserver<Int64Value> responseObserver) {
    responseObserver.onNext(
        Int64Value.newBuilder().setValue(blockService.getOffset(request)).build());
    responseObserver.onCompleted();
  }
}
