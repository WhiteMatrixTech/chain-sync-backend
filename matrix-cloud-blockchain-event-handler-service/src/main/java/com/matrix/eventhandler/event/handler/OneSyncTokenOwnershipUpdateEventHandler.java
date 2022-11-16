package com.matrix.eventhandler.event.handler;

import com.matrix.common.model.Address;
import com.matrix.eventhandler.model.BlockChainEvent;
import com.matrix.eventhandler.model.EvmEvent;
import com.matrix.metadata.dao.NftCollectionDao;
import com.matrix.metadata.model.NftCollection;
import java.math.BigInteger;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
@Component
@Profile({"alpha", "beta", "prod", "local"})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OneSyncTokenOwnershipUpdateEventHandler implements BlockchainEventHandler {

  private static final HttpHeaders REQUEST_HEADERS = new HttpHeaders();

  static {
    REQUEST_HEADERS.add("x-api-key", "faq6bxckOz830r0m");
  }

  public static final String TRANSFER = "Transfer";

  private final NftCollectionDao nftCollectionDao;

  private final RestTemplate restTemplate;

  @Value("${oneSync.metadata.endpoint:http://matrix-cloud-metadata-service:8080/metadata/api}")
  String metadataUrl;

  @Override
  public boolean isApplicable(final BlockChainEvent blockChainEvent) {
    if (!(blockChainEvent instanceof EvmEvent)) {
      return false;
    }
    if (TRANSFER.equals(blockChainEvent.getEventName())) {
      final NftCollection nftCollection =
          nftCollectionDao.getNftCollection(blockChainEvent.getContract());
      return nftCollection != null;
    }
    return false;
  }

  @Override
  public void processBlockChainEvent(final BlockChainEvent blockChainEvent) {
    notifyTokenOwnershipUpdate((EvmEvent) blockChainEvent);
  }

  private void notifyTokenOwnershipUpdate(final EvmEvent blockChainEvent) {
    log.info(
        "[OneSyncTokenOwnershipUpdateEventHandler.notifyTokenOwnershipUpdate] start, blockChainEvent is {}",
        blockChainEvent);
    final int tokenId =
        ((BigInteger) blockChainEvent.getPayload().get("tokenId").getValue()).intValue();

    final Address from =
        Address.fromAddressAndChainId(
            blockChainEvent.getPayload().get("from").toString(),
            blockChainEvent.getContract().getChainId());

    final Address to =
        Address.fromAddressAndChainId(
            blockChainEvent.getPayload().get("to").toString(),
            blockChainEvent.getContract().getChainId());

    final String url =
        this.metadataUrl
            + "/internal/v1/contracts/"
            + blockChainEvent.getContract()
            + "/metadata/tokens/"
            + tokenId
            + "/ownership";
    final Map<String, ?> params =
        Map.of(
            "to",
            to.toString(),
            "from",
            from.toString(),
            "blockNumber",
            blockChainEvent.getBlockNumber());
    final HttpEntity<Map<String, ?>> entity = new HttpEntity<>(params, REQUEST_HEADERS);
    // TODO rpc
    restTemplate.postForEntity(url, entity, Void.class);
  }
}
