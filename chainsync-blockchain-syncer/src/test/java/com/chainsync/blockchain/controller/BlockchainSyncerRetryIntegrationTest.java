package com.chainsync.blockchain.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.chainsync.blockchain.model.RetryRequest;
import com.chainsync.blockchain.model.BlockChainSyncServiceGrpc;
import com.chainsync.blockchain.model.BlockChainSyncServiceGrpc.BlockChainSyncServiceBlockingStub;
import com.chainsync.blockchain.model.SyncResult;
import com.chainsync.blockchain.model.SyncStatus;
import com.chainsync.common.model.ChainName;
import com.chainsync.common.model.ChainType;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BlockchainSyncerRetryIntegrationTest {
  @Rule public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
  @Resource BlockChainSyncerController controller;

  @SneakyThrows
  @Test
  void test() {
    // Generate a unique in-process server name.
    String serverName = InProcessServerBuilder.generateName();

    // Create a server, add service, start, and register for automatic graceful shutdown.
    grpcCleanup.register(
        InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(controller)
            .build()
            .start());

    BlockChainSyncServiceBlockingStub blockingStub =
        BlockChainSyncServiceGrpc.newBlockingStub(
            // Create a client channel and register for automatic graceful shutdown.
            grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build()));

    retryTask(blockingStub);
  }

  private void retryTask(BlockChainSyncServiceBlockingStub blockingStub) {
    SyncResult result =
        blockingStub.retrySyncTask(
            RetryRequest.newBuilder()
                .setChainType(ChainType.ethereum.name())
                .setChainName(ChainName.mainnet.name())
                .setCount(1)
                .build());

    assertEquals(SyncStatus.SUCCESS.name(), result.getStatus());
  }
}
