package com.matrix.blockchain.retriever;

import com.google.common.collect.Maps;
import com.matrix.blockchain.exception.SyncInitFailedException;
import com.matrix.blockchain.model.BlockRange;
import com.matrix.blockchain.model.BlockchainType;
import com.matrix.common.model.ChainName;
import io.grpc.Codec;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.security.CallCredentialsHelper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import proto.v1.BlockEventsResponseEvent;
import proto.v1.QueryAllEventByBlockRangeRequest;
import proto.v1.QueryAllEventByBlockRangeResponse;
import proto.v1.QueryLatestBlockHeightRequest;
import proto.v1.QueryLatestBlockHeightResponse;
import proto.v1.SporkGrpc.SporkBlockingStub;

/**
 * @author luyuanheng
 */
@Log4j2
@Component
public class FlowEventRetriever implements InitializingBean {

  private static final String METADATA_STAGE = "stage";

  @GrpcClient("matrix-cloud-flow-service")
  SporkBlockingStub sporkBlockingStub;

  private Map<String, SporkBlockingStub> sporkBlockingStubMap = Maps.newHashMap();

  private static final String GZIP = new Codec.Gzip().getMessageEncoding();

  public long getBlockHeight(String chainId) {
    try {
      return this.getBlockHeightInternal(chainId);
    } catch (final Exception e) {
      log.error("[getBlockHeight] Error: ", e);
      throw new SyncInitFailedException(e.getMessage(), e);
    }
  }

  public List<BlockEventsResponseEvent> retrieveEvents(final BlockRange blockRange) {
    try {
      return this.retrieveEventsInternal(blockRange);
    } catch (final Exception e) {
      log.error("[retrieveEvents] Error: ", e);
      throw new SyncInitFailedException(e.getMessage(), e);
    }
  }

  @SneakyThrows
  private List<BlockEventsResponseEvent> retrieveEventsInternal(BlockRange blockRange) {
    QueryAllEventByBlockRangeResponse response =
        getSporkBlockingStub(blockRange.getChainId())
            .queryAllEventByBlockRange(
                QueryAllEventByBlockRangeRequest.newBuilder()
                    .setStart(blockRange.getFrom())
                    .setEnd(blockRange.getTo())
                    .build());

    return response.getEventsList();
  }

  @SneakyThrows
  private long getBlockHeightInternal(String chainId) {
    QueryLatestBlockHeightResponse response =
        getSporkBlockingStub(chainId)
            .queryLatestBlockHeight(QueryLatestBlockHeightRequest.newBuilder().build());

    return response.getLatestBlockHeight();
  }

  private SporkBlockingStub getSporkBlockingStub(String chainId) {
    if (!sporkBlockingStubMap.containsKey(chainId)) {
      throw new UnsupportedOperationException("not support flow chainId: " + chainId);
    }

    return sporkBlockingStubMap.get(chainId);
  }

  @Override
  public void afterPropertiesSet() {
    sporkBlockingStub =
        this.sporkBlockingStub
            .withMaxOutboundMessageSize(Integer.MAX_VALUE)
            .withMaxInboundMessageSize(Integer.MAX_VALUE)
            .withCompression(GZIP);

    Metadata testNetMetadata = new Metadata();
    testNetMetadata.put(
        Key.of(METADATA_STAGE, Metadata.ASCII_STRING_MARSHALLER), ChainName.testnet.name());
    sporkBlockingStubMap.put(
        BlockchainType.FLOW_TEST_NET.getChainId(),
        sporkBlockingStub.withCallCredentials(
            CallCredentialsHelper.authorizationHeaders(testNetMetadata)));

    Metadata mainNetMetadata = new Metadata();
    mainNetMetadata.put(
        Key.of(METADATA_STAGE, Metadata.ASCII_STRING_MARSHALLER), ChainName.mainnet.name());
    sporkBlockingStubMap.put(
        BlockchainType.FLOW_MAIN_NET.getChainId(),
        sporkBlockingStub.withCallCredentials(
            CallCredentialsHelper.authorizationHeaders(mainNetMetadata)));
  }
}
