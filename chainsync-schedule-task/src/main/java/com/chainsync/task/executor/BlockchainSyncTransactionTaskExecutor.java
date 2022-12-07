package com.chainsync.task.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.chainsync.blockchain.model.BlockChainSyncServiceGrpc.BlockChainSyncServiceBlockingStub;
import com.chainsync.blockchain.model.SyncTransactionRequest;
import com.chainsync.task.model.TaskDef;
import com.chainsync.task.model.TaskType;
import com.chainsync.task.service.util.JacksonJsonUtils;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

/**
 * Task executor of sync transaction task.
 *
 * @author ShenYang
 */
@Component
public class BlockchainSyncTransactionTaskExecutor implements MatrixTaskExecutor {

  /** Blockchain syncer client. */
  @GrpcClient("matrix-cloud-blockchain-syncer")
  private BlockChainSyncServiceBlockingStub syncServiceBlockingStub;

  @Override
  @SuppressWarnings({"ResultOfMethodCallIgnored"})
  public String execute(final TaskDef taskDef) {
    final JsonNode params = JacksonJsonUtils.readTree(taskDef.getParams());
    syncServiceBlockingStub.syncTransaction(
        SyncTransactionRequest.newBuilder()
            .setTaskId(taskDef.getTaskName())
            .setStart(params.get("start").asLong())
            .setEnd(params.get("end").asLong())
            .setStep(params.get("step").asInt())
            .build());
    return "ok";
  }

  @Override
  public boolean isApplicable(final TaskDef taskDef) {
    return TaskType.SYNC_TRANSACTION.name().equals(taskDef.getTaskType());
  }
}
