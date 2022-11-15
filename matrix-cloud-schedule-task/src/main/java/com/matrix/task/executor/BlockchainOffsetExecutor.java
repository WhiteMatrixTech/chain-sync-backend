package com.matrix.task.executor;

import com.google.protobuf.Int64Value;
import com.google.protobuf.util.JsonFormat;
import com.matrix.blockchain.model.BlockChainSyncServiceGrpc.BlockChainSyncServiceBlockingStub;
import com.matrix.blockchain.model.GetOffsetRequest;
import com.matrix.blockchain.model.GetOffsetRequest.Builder;
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
public class BlockchainOffsetExecutor implements MatrixTaskExecutor {

  @GrpcClient("matrix-cloud-blockchain-syncer")
  BlockChainSyncServiceBlockingStub syncServiceBlockingStub;

  @SneakyThrows
  @Override
  public String execute(TaskDef taskDef) {
    Builder builder = GetOffsetRequest.newBuilder();
    JsonFormat.parser().merge(taskDef.getParams(), builder);

    Int64Value result =
        syncServiceBlockingStub.withDeadlineAfter(30, TimeUnit.SECONDS).getOffset(builder.build());
    return result.toString();
  }

  @Override
  public boolean isApplicable(TaskDef taskDef) {
    return TaskType.OFFSET.name().equalsIgnoreCase(taskDef.getTaskType());
  }
}
