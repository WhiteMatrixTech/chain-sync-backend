package com.matrix.task.executor;

import com.google.protobuf.util.JsonFormat;
import com.matrix.blockchain.model.BlockChainSyncServiceGrpc.BlockChainSyncServiceBlockingStub;
import com.matrix.blockchain.model.SyncResult;
import com.matrix.blockchain.model.SyncStep;
import com.matrix.blockchain.model.SyncStep.Builder;
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
public class BlockchainSyncTaskExecutor implements MatrixTaskExecutor {

  @GrpcClient("matrix-cloud-blockchain-syncer")
  BlockChainSyncServiceBlockingStub syncServiceBlockingStub;

  @SneakyThrows
  @Override
  public String execute(TaskDef taskDef) {
    Builder builder = SyncStep.newBuilder();
    JsonFormat.parser().merge(taskDef.getParams(), builder);

    SyncResult result =
        syncServiceBlockingStub
            .withDeadlineAfter(30, TimeUnit.SECONDS)
            .runSyncTask(builder.build());
    return result.getErrorMessage();
  }

  @Override
  public boolean isApplicable(TaskDef taskDef) {
    return TaskType.SYNC_BLOCKCHAIN.name().equalsIgnoreCase(taskDef.getTaskType());
  }
}
