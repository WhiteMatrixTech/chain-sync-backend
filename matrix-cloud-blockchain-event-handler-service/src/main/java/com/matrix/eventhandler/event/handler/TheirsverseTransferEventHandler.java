package com.matrix.eventhandler.event.handler;

import com.matrix.common.model.Address;
import com.matrix.common.model.TokenId;
import com.matrix.eventhandler.model.BlockChainEvent;
import com.matrix.eventhandler.model.EvmEvent;
import com.matrix.metadata.dao.NftCollectionDao;
import com.matrix.metadata.model.NftCollection;
import com.matrix.theirsverse.model.ChangeOwnershipRequestDTO;
import com.matrix.theirsverse.model.TheirsverseMetadataServiceGrpc.TheirsverseMetadataServiceBlockingStub;
import java.math.BigInteger;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author reimia
 */
@Log4j2
@Component
@Profile({"alpha-testnet", "prod-mainnet", "beta-testnet", "local"})
public class TheirsverseTransferEventHandler implements BlockchainEventHandler {

  @Resource NftCollectionDao nftCollectionDao;

  @GrpcClient("matrix-cloud-theirsverse-service")
  TheirsverseMetadataServiceBlockingStub blockingStub;

  public static final String TRANSFER = "Transfer";

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
    final EvmEvent event = (EvmEvent) blockChainEvent;
    log.info("[ThiersverseTransferEventHandler] blockChainEvent is {}", blockChainEvent);
    final int tokenId = ((BigInteger) event.getPayload().get("tokenId").getValue()).intValue();

    final Address from =
        Address.fromAddressAndChainId(
            event.getPayload().get("from").toString(), event.getContract().getChainId());

    final Address to =
        Address.fromAddressAndChainId(
            event.getPayload().get("to").toString(), event.getContract().getChainId());
    log.info(
        "[ThiersverseTransferEventHandler] transfer event parse success, tokenId: [{}], from: [{}], to: [{}]",
        tokenId,
        from,
        to);
    blockingStub.changeOwnership(
        ChangeOwnershipRequestDTO.newBuilder()
            .setContractAddress(event.getContract().toString())
            .setFrom(from.toString())
            .setTo(to.toString())
            .setTokenId(TokenId.from(tokenId).toProtobuf())
            .setBlockNumber(event.getBlockNumber())
            .build());
  }
}
