package com.chainsync.etl.service.impl;

import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.chainsync.common.model.Address;
import com.chainsync.etl.dao.TokenDao;
import com.chainsync.etl.model.Token;
import com.chainsync.etl.service.TokenService;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Service;
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
@Service
public class TokenServiceImpl implements TokenService {

  @Resource TokenDao tokenDao;

  @Resource
  RestTemplate restTemplate;

  private final Web3j web3j =
      Web3j.build(
          new HttpService("https://eth-mainnet.g.alchemy.com/v2/yoJCyg5XsbdD9u072GIASQJqNNYdpJlW"));

  @Override
  public List<Token> scanEthereumToken() {
    return tokenDao.scanWithLimit(50);
  }

  @Override
  public List<Token> queryEthereumToken(final String address, final String tokenId) {
    if (tokenId != null) {
      final Token item = tokenDao.getItem(address, tokenId);
      if (item == null) {
        return List.of();
      }
      return List.of(item);
    }
    return tokenDao.queryByPartitionKey(address);
  }

  @Override
  public List<Token> queryEthereumTokenByOwner(final String owner) {
    return tokenDao.queryByPartitionKeyOnGsi(Token.INDEX_OWNER, owner);
  }

  @Override
  public List<Token> scanFlowToken() {
    return List.of();
  }

  @Override
  public List<Token> queryFlowToken(final String address, final String tokenId) {
    return List.of();
  }

  @Override
  public List<Token> queryFlowTokenByOwner(final String owner) {
    return List.of();
  }

  @SneakyThrows
  @Override
  public void refreshEthereumTokenMetadata(final Address address, final String tokenId) {
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
    final String tokenURI = (String) someTypes.get(0).getValue();
    log.info(
        "[refreshEthereumTokenMetadata] metadata uri resolve success, address:{}, token:{}, uri: {}",
        address,
        tokenId,
        tokenURI);
    tokenDao.update(
        address.getCanonicalAddress(),
        tokenId,
        List.of(new AttributeUpdate(Token.ATTR_TOKEN_METADATA_URI).put(tokenURI)));
    if (UrlValidator.getInstance().isValid(tokenURI)) {
      final String tokenMetadataRaw = restTemplate.getForObject(tokenURI, String.class);
      tokenDao.update(
          address.getCanonicalAddress(),
          tokenId,
          List.of(new AttributeUpdate(Token.ATTR_TOKEN_METADATA_RAW).put(tokenMetadataRaw)));
    }
  }

}
