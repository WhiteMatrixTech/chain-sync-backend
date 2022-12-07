package com.matrix.task.executor;

import com.google.protobuf.util.JsonFormat;
import com.matrix.blockchain.model.BlockChainSyncServiceGrpc.BlockChainSyncServiceBlockingStub;
import com.matrix.blockchain.model.RetryRequest;
import com.matrix.blockchain.model.RetryRequest.Builder;
import com.matrix.blockchain.model.SyncResult;
import com.matrix.task.model.TaskDef;
import com.matrix.task.model.TaskType;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

/**
 * @author luyuanheng
 */
@Component
public class BlockchainSyncRetryTaskExecutor implements MatrixTaskExecutor {

  @GrpcClient("matrix-cloud-blockchain-syncer")
  BlockChainSyncServiceBlockingStub syncServiceBlockingStub;

  @SneakyThrows
  @Override
  public String execute(TaskDef taskDef) {
    Builder builder = RetryRequest.newBuilder();
    JsonFormat.parser().merge(taskDef.getParams(), builder);

    SyncResult result =
        syncServiceBlockingStub
            .withDeadlineAfter(30, TimeUnit.SECONDS)
            .retrySyncTask(builder.build());
    return result.getStatus();
  }

  @Override
  public boolean isApplicable(TaskDef taskDef) {
    return TaskType.SYNC_RETRY_TASK.name().equalsIgnoreCase(taskDef.getTaskType());
  }
}
