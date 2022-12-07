package com.matrix.blockchain.handler;

import com.google.common.collect.Maps;
import com.matrix.blockchain.exception.SyncInitFailedException;
import com.matrix.blockchain.model.BlockList;
import com.matrix.blockchain.model.BlockchainType;
import com.matrix.common.model.ChainName;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.security.CallCredentialsHelper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import schedule.proto.LatestBlockHeightReq;
import schedule.proto.LatestBlockHeightResp;
import schedule.proto.ScheduleGrpc.ScheduleBlockingStub;
import schedule.proto.ScheduleJobReq;
import schedule.proto.ScheduleJobResp;

/**
 * @author luyuanheng
 */
@Log4j2
@Component
public class FlowSyncHandler implements InitializingBean {

  private static final String METADATA_STAGE = "stage";

  @GrpcClient("matrix-cloud-flow-service")
  ScheduleBlockingStub scheduleBlockingStub;

  private Map<String, ScheduleBlockingStub> scheduleBlockingStubMap = Maps.newHashMap();

  public long getBlockHeight(String chainId) {
    try {
      return this.getBlockHeightInternal(chainId);
    } catch (final Exception e) {
      log.error("[getBlockHeight] Error: ", e);
      throw new SyncInitFailedException(e.getMessage(), e);
    }
  }

  public ScheduleJobResp handleSyncRequest(final BlockList blockList) {
    try {
      return this.handleSyncRequestInternal(blockList);
    } catch (final Exception e) {
      log.error("[handle request] Error: ", e);
      throw new SyncInitFailedException(e.getMessage(), e);
    }
  }

  @SneakyThrows
  private ScheduleJobResp handleSyncRequestInternal(final BlockList blockList) {
    ScheduleJobResp response =
        getScheduleBlockingStub(blockList.getChainId())
            .scheduleJob(
                ScheduleJobReq.newBuilder().addAllHeights(blockList.getBlockNumbersList()).build());

    return response;
  }

  @SneakyThrows
  private long getBlockHeightInternal(String chainId) {
    LatestBlockHeightResp response =
        getScheduleBlockingStub(chainId)
            .latestBlockHeight(LatestBlockHeightReq.newBuilder().build());

    return response.getHeight();
  }

  private ScheduleBlockingStub getScheduleBlockingStub(String chainId) {
    if (!scheduleBlockingStubMap.containsKey(chainId)) {
      throw new UnsupportedOperationException("not support flow chainId: " + chainId);
    }

    return scheduleBlockingStubMap.get(chainId);
  }

  @Override
  public void afterPropertiesSet() {
    scheduleBlockingStub =
        this.scheduleBlockingStub
            .withMaxOutboundMessageSize(Integer.MAX_VALUE)
            .withMaxInboundMessageSize(Integer.MAX_VALUE);

    Metadata testNetMetadata = new Metadata();
    testNetMetadata.put(
        Key.of(METADATA_STAGE, Metadata.ASCII_STRING_MARSHALLER), ChainName.testnet.name());
    scheduleBlockingStubMap.put(
        BlockchainType.FLOW_TEST_NET.getChainId(),
        scheduleBlockingStub.withCallCredentials(
            CallCredentialsHelper.authorizationHeaders(testNetMetadata)));

    Metadata mainNetMetadata = new Metadata();
    mainNetMetadata.put(
        Key.of(METADATA_STAGE, Metadata.ASCII_STRING_MARSHALLER), ChainName.mainnet.name());
    scheduleBlockingStubMap.put(
        BlockchainType.FLOW_MAIN_NET.getChainId(),
        scheduleBlockingStub.withCallCredentials(
            CallCredentialsHelper.authorizationHeaders(mainNetMetadata)));
  }
}
