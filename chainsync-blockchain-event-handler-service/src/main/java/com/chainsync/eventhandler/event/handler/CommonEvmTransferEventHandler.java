package com.chainsync.eventhandler.event.handler;

import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.chainsync.eventhandler.model.BlockChainEvent;
import com.chainsync.eventhandler.model.Token;
import com.chainsync.eventhandler.module.ThreadPoolExecutorModule;
import com.chainsync.common.exception.ErrorCodedException;
import com.chainsync.common.model.Address;
import com.chainsync.common.response.ResultCode;
import com.chainsync.eventhandler.dao.TokenDao;
import com.chainsync.eventhandler.model.EvmEvent;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;

/**
 * @author reimia
 */
@Log4j2
@Component
@Profile({"alpha-mainnet", "local"})
public class CommonEvmTransferEventHandler implements BlockchainEventHandler, InitializingBean {

  public static final String GROUP = "Default";
  public static final String TRANSFER = "Transfer";

  @Resource TokenDao tokenDao;

  @Resource RestTemplate restTemplate;

  @Resource RetryTemplate retryTemplate;

  @Resource(name = ThreadPoolExecutorModule.CACHED_EXECUTOR)
  ThreadPoolExecutor executor;

  @Value("${blockchain.ethereum-provider-endpoint}")
  String web3jEndPoint;

  private Web3j web3j;

  @Override
  public String getGroup() {
    return GROUP;
  }

  @Override
  public boolean isApplicable(final BlockChainEvent blockChainEvent) {
    if (!(blockChainEvent instanceof EvmEvent)) {
      return false;
    }
    return TRANSFER.equals(blockChainEvent.getEventName());
  }

  @Override
  public void processBlockChainEvent(final BlockChainEvent blockChainEvent) {
    final EvmEvent event = (EvmEvent) blockChainEvent;
    final String tokenId = event.getPayload().get("tokenId").getValue().toString();
    final Address contract = blockChainEvent.getContract();
    final Address from =
        Address.fromAddressAndChainId(
            event.getPayload().get("from").toString(), event.getContract().getChainId());
    final Address to =
        Address.fromAddressAndChainId(
            event.getPayload().get("to").toString(), event.getContract().getChainId());
    log.info(
        "[CommonEvmTransferEventHandler] transfer event parse success, contract: [{}] tokenId: [{}], from: [{}], to: [{}]",
        contract,
        tokenId,
        from,
        to);
    final Token item = tokenDao.getItem(contract.getCanonicalAddress(), tokenId);
    if (item == null) {
      log.info(
          "[CommonEvmTransferEventHandler] no token find, save and update metadata, contract: [{}] tokenId: [{}]",
          contract,
          tokenId);
      tokenDao.putItem(
          Token.builder()
              .address(contract.getCanonicalAddress())
              .tokenId(tokenId)
              .owner(to.getCanonicalAddress())
              .build());
      executor.submit(() -> updateMetadata(contract, tokenId));
      return;
    }
    log.info(
        "[CommonEvmTransferEventHandler] token is existed, only update owner, contract: [{}] tokenId: [{}]",
        contract,
        tokenId);
    tokenDao.update(
        contract.getCanonicalAddress(),
        tokenId,
        List.of(new AttributeUpdate(Token.ATTR_OWNER).put(to.getCanonicalAddress())));
  }

  @SneakyThrows
  public void updateMetadata(final Address address, final String tokenId) {
    log.info(
        "[CommonEvmTransferEventHandler] start to update metadata, address: {}, tokenId: {}",
        address,
        tokenId);
    final String tokenURI =
        retryTemplate.execute(
            context -> {
              final Function function =
                  new Function(
                      "tokenURI",
                      List.of(new Uint256(new BigInteger(tokenId))),
                      List.of(new TypeReference<Utf8String>() {}));

              final String encodedFunction = FunctionEncoder.encode(function);
              final EthCall response =
                  web3j
                      .ethCall(
                          Transaction.createEthCallTransaction(
                              "0x0000000000000000000000000000000000000000",
                              address.getNormalizedAddress(),
                              encodedFunction),
                          DefaultBlockParameterName.LATEST)
                      .sendAsync()
                      .get();

              final List<Type> someTypes =
                  FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
              return (String) someTypes.get(0).getValue();
            },
            context -> {
              log.error(
                  "retry 3 times failed when get tokenURI, address: {}, token: {}",
                  address,
                  tokenId,
                  context.getLastThrowable());
              throw new ErrorCodedException(
                  ResultCode.INTERNAL_SERVER_ERROR,
                  "retry 3 times failed when get tokenURI, address: "
                      + address
                      + "token: "
                      + tokenId,
                  context.getLastThrowable());
            });
    log.info(
        "[CommonEvmTransferEventHandler] metadata uri resolve success, address:{}, token:{}, uri: {}",
        address,
        tokenId,
        tokenURI);
    tokenDao.update(
        address.getCanonicalAddress(),
        tokenId,
        List.of(new AttributeUpdate(Token.ATTR_TOKEN_METADATA_URI).put(tokenURI)));

    // todo not handle ethereum ipfs now
    if (UrlValidator.getInstance().isValid(tokenURI)) {
      final String tokenMetadataRaw =
          retryTemplate.execute(
              context -> restTemplate.getForObject(tokenURI, String.class),
              context -> {
                log.error(
                    "retry 3 times failed when get metadata from tokenURI, tokenURI: {}",
                    tokenURI,
                    context.getLastThrowable());
                throw new ErrorCodedException(
                    ResultCode.INTERNAL_SERVER_ERROR,
                    "retry 3 times failed when get metadata from tokenURI, tokenURI: " + tokenURI,
                    context.getLastThrowable());
              });
      tokenDao.update(
          address.getCanonicalAddress(),
          tokenId,
          List.of(new AttributeUpdate(Token.ATTR_TOKEN_METADATA_RAW).put(tokenMetadataRaw)));
    }
  }

  @Override
  public void afterPropertiesSet() {
    final String hostname = "127.0.0.1";
    final int port = 7890;
    final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port));
    final OkHttpClient client = new OkHttpClient.Builder().proxy(proxy).build();
    web3j = Web3j.build(new HttpService(web3jEndPoint, client));
  }
}
