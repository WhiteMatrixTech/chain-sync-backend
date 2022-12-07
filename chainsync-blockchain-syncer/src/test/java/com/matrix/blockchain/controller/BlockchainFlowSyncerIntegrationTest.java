package com.matrix.blockchain.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.matrix.blockchain.model.BlockChainSyncServiceGrpc;
import com.matrix.blockchain.model.BlockChainSyncServiceGrpc.BlockChainSyncServiceBlockingStub;
import com.matrix.blockchain.model.BlockRange;
import com.matrix.blockchain.model.SyncResult;
import com.matrix.blockchain.model.SyncStatus;
import com.matrix.common.model.ChainId;
import com.matrix.common.model.ChainName;
import com.matrix.common.model.ChainType;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BlockchainFlowSyncerIntegrationTest {
  private static final Long BLOCK_FROM = 32281646L;
  private static final Long BLOCK_TO = 32281655L;
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

    flowSync(blockingStub);
  }

  private void flowSync(BlockChainSyncServiceBlockingStub blockingStub) {
    SyncResult flowResult =
        blockingStub.rangeSyncTask(
            BlockRange.newBuilder()
                .setChainType(ChainType.flow.name())
                .setChainName(ChainName.mainnet.name())
                .setChainId(
                    ChainId.builder()
                        .chainName(ChainName.mainnet)
                        .chainType(ChainType.flow)
                        .build()
                        .toString())
                .setFrom(BLOCK_FROM)
                .setTo(BLOCK_TO)
                .build());

    assertEquals(SyncStatus.SUCCESS.name(), flowResult.getStatus());
  }
}
