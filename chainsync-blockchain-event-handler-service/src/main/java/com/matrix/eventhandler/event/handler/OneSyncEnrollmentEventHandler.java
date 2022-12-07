package com.matrix.eventhandler.event.handler;

import com.matrix.common.model.Address;
import com.matrix.common.model.PolygonAddress;
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
public class OneSyncEnrollmentEventHandler implements BlockchainEventHandler {

  public static final String GROUP = "OneSync";

  private static final HttpHeaders REQUEST_HEADERS = new HttpHeaders();

  static {
    REQUEST_HEADERS.add("x-api-key", "faq6bxckOz830r0m");
  }

  public static final String TOKEN_LOCKED = "tokenLocked";
  public static final String TOKEN_UNLOCKED = "tokenUnlocked";

  private final NftCollectionDao nftCollectionDao;

  private final RestTemplate restTemplate;

  @Value("${oneSync.metadata.endpoint:http://matrix-cloud-metadata-service:8080/metadata/api}")
  String metadataUrl;

  @Override
  public String getGroup() {
    return GROUP;
  }

  @Override
  public boolean isApplicable(final BlockChainEvent blockChainEvent) {
    if (!(blockChainEvent instanceof EvmEvent)) {
      return false;
    }
    if (TOKEN_LOCKED.equals(blockChainEvent.getEventName())
        || TOKEN_UNLOCKED.equals(blockChainEvent.getEventName())) {
      final NftCollection nftCollection =
          nftCollectionDao.getNftCollection(blockChainEvent.getContract());
      return nftCollection != null;
    }
    return false;
  }

  @Override
  public void processBlockChainEvent(final BlockChainEvent blockChainEvent) {
    log.info(
        "[OneSyncEnrollmentEventHandler.processBlockChainEvent] start, blockChainEvent is : {}",
        blockChainEvent);
    if (TOKEN_LOCKED.equals(blockChainEvent.getEventName())) {
      this.notifyTokenEnrollment((EvmEvent) blockChainEvent, "ENROLLED");
    } else if (TOKEN_UNLOCKED.equals(blockChainEvent.getEventName())) {
      this.notifyTokenEnrollment((EvmEvent) blockChainEvent, "UN_ENROLLED");
    } else {
      log.error("Not a enrollment event, isApplicable work wrong");
    }
  }

  private void notifyTokenEnrollment(
      final EvmEvent blockChainEvent, final String enrollmentStatus) {
    log.info(
        "[OneSyncEnrollmentEventHandler.notifyTokenEnrollment] start, blockChainEvent is : {}, enrollmentStatus is : {}",
        blockChainEvent,
        enrollmentStatus);
    final Address fullAddress = blockChainEvent.getContract();
    final int tokenId =
        ((BigInteger) blockChainEvent.getPayload().get("tokenId").getValue()).intValue();
    final long version =
        ((BigInteger) blockChainEvent.getPayload().get("version").getValue()).longValue();
    final Address owner =
        new PolygonAddress(
            blockChainEvent.getPayload().get("owner").toString(), fullAddress.getChainId());
    final long blockHeight =
        ((BigInteger) blockChainEvent.getPayload().get("blockHeight").getValue()).longValue();

    final String url =
        this.metadataUrl
            + "/internal/v1/contracts/"
            + blockChainEvent.getContract()
            + "/metadata/tokens/"
            + tokenId
            + "/enrollment";
    final Map<String, ?> params =
        Map.of(
            "enrollmentStatus",
            enrollmentStatus,
            "version",
            version,
            "owner",
            owner.toString(),
            "blockHeight",
            blockHeight);
    final HttpEntity<Map<String, ?>> entity = new HttpEntity<>(params, REQUEST_HEADERS);
    // TODO rpc
    restTemplate.postForEntity(url, entity, Void.class);
  }
}
