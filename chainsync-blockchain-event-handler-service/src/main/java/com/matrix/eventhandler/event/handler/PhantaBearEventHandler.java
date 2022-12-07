package com.matrix.eventhandler.event.handler;

import com.matrix.common.model.ChainType;
import com.matrix.eventhandler.model.BlockChainEvent;
import com.matrix.eventhandler.model.EvmEvent;
import com.matrix.eventhandler.model.PhantaciNotify;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author reimia
 */
@Component
@Profile({"alpha", "beta", "prod", "local"})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PhantaBearEventHandler implements BlockchainEventHandler {

  public static final String GROUP = "Phantaci";

  private static final HttpHeaders REQUEST_HEADERS = new HttpHeaders();

  static {
    REQUEST_HEADERS.add("X-API-KEY", "1tJMP2xWp67YRnA4");
  }

  public static final String TRANSFER = "Transfer";

  private final RestTemplate restTemplate;

  // TODO should in table
  @Value("${phantaBear.address}")
  String phantaBearAddress;

  @Value("${phantaBear.metadata.endpoint:http://matrix-metadata-service:8080/metadata/api/v1/nft}")
  String metadataUrl;

  @Override
  public String getGroup() {
    return GROUP;
  }

  @Override
  public boolean isApplicable(final BlockChainEvent blockChainEvent) {
    if (blockChainEvent instanceof EvmEvent
        && blockChainEvent.getContract().getChainId().getChainType().equals(ChainType.ethereum)
        && blockChainEvent.getEventName().equals(TRANSFER)) {
      return blockChainEvent.getContract().getNormalizedAddress().equals(phantaBearAddress);
    }
    return false;
  }

  @Override
  public void processBlockChainEvent(final BlockChainEvent blockChainEvent) {
    final EvmEvent event = (EvmEvent) blockChainEvent;
    final Map<String, Object> payload =
        event.getPayload().entrySet().stream()
            .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getValue()));
    final PhantaciNotify phantaciNotify =
        PhantaciNotify.builder()
            .address(event.getContract().toString())
            .blockNumber(event.getBlockNumber())
            .eventName(event.getEventName())
            .blockTimestamp(event.getBlockTimeStamp())
            .payload(payload)
            .build();
    final HttpEntity<PhantaciNotify> requestEntity =
        new HttpEntity<>(phantaciNotify, REQUEST_HEADERS);

    // TODO rpc
    this.restTemplate.postForEntity(
        this.metadataUrl + "/notifyEthEvent", requestEntity, String.class);
  }
}
