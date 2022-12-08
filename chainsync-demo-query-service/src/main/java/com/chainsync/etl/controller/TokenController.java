package com.chainsync.etl.controller;

import com.chainsync.common.model.Address;
import com.chainsync.common.model.ChainId;
import com.chainsync.common.model.ChainName;
import com.chainsync.common.model.ChainType;
import com.chainsync.common.model.EthereumAddress;
import com.chainsync.common.model.FlowAddress;
import com.chainsync.etl.model.Token;
import com.chainsync.etl.model.TokenResponse;
import com.chainsync.etl.model.response.QueryTokenResponse;
import com.chainsync.etl.service.TokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author reimia
 */
@Log4j2
@Api(tags = "query token api")
@RestController
@RequestMapping("/v1/token")
public class TokenController {

  @Resource TokenService tokenService;

  @ApiOperation(value = "query ethereum token data")
  @GetMapping("/ethereum")
  public QueryTokenResponse queryEthereumToken(
      @RequestParam(required = false) final String address,
      @RequestParam(required = false) final String tokenId) {
    if (address == null && tokenId == null) {
      final List<Token> tokens = tokenService.scanEthereumToken();
      final List<TokenResponse> collect =
          tokens.parallelStream().map(TokenResponse::new).collect(Collectors.toList());
      return QueryTokenResponse.builder().tokens(collect).build();
    }
    final List<Token> tokens =
        tokenService.queryEthereumToken(convertStringToEthAddress(address).toString(), tokenId);
    final List<TokenResponse> collect =
        tokens.parallelStream().map(TokenResponse::new).collect(Collectors.toList());
    return QueryTokenResponse.builder().tokens(collect).build();
  }

  @ApiOperation(value = "query ethereum token data by owner")
  @GetMapping("/ethereum/owner/{owner}")
  public QueryTokenResponse queryEthereumTokenByOwner(@PathVariable final String owner) {
    final List<Token> tokens =
        tokenService.queryEthereumTokenByOwner(convertStringToEthAddress(owner).toString());
    final List<TokenResponse> collect =
        tokens.parallelStream().map(TokenResponse::new).collect(Collectors.toList());
    return QueryTokenResponse.builder().tokens(collect).build();
  }

  @ApiOperation(value = "query flow token data")
  @GetMapping("/flow")
  public QueryTokenResponse queryFlowToken(
      @RequestParam(required = false) final String address,
      @RequestParam(required = false) final String tokenId) {
    if (address == null && tokenId == null) {
      final List<Token> tokens = tokenService.scanFlowToken();
      final List<TokenResponse> collect =
          tokens.parallelStream().map(TokenResponse::new).collect(Collectors.toList());
      return QueryTokenResponse.builder().tokens(collect).build();
    }
    final List<Token> tokens =
        tokenService.queryFlowToken(convertStringToFlowAddress(address).toString(), tokenId);
    final List<TokenResponse> collect =
        tokens.parallelStream().map(TokenResponse::new).collect(Collectors.toList());
    return QueryTokenResponse.builder().tokens(collect).build();
  }

  @ApiOperation(value = "query flow token data by owner")
  @GetMapping("/flow/owner/{owner}")
  public QueryTokenResponse queryFlowTokenByOwner(@PathVariable final String owner) {
    final List<Token> tokens =
        tokenService.queryFlowTokenByOwner(convertStringToFlowAddress(owner).toString());
    final List<TokenResponse> collect =
        tokens.parallelStream().map(TokenResponse::new).collect(Collectors.toList());
    return QueryTokenResponse.builder().tokens(collect).build();
  }

  @ApiOperation(value = "refresh ethereum token metadata")
  @PostMapping("/ethereum/refresh/{address}/{tokenId}")
  public void refreshEthereumTokenMetadata(
      @PathVariable final String address, @PathVariable final String tokenId) {
    tokenService.refreshEthereumTokenMetadata(Address.fromFullAddress(address), tokenId);
  }

  private Address convertStringToEthAddress(final String address) {
    if (address == null) {
      throw new IllegalArgumentException("invalid address");
    }
    Address fullAddress;
    try {
      fullAddress = Address.fromFullAddress(address);
    } catch (final Exception e) {
      fullAddress =
          new EthereumAddress(
              address,
              ChainId.builder().chainType(ChainType.ethereum).chainName(ChainName.mainnet).build());
    }
    return fullAddress;
  }

  private Address convertStringToFlowAddress(final String address) {
    if (address == null) {
      throw new IllegalArgumentException("invalid address");
    }
    Address fullAddress;
    try {
      fullAddress = Address.fromFullAddress(address);
    } catch (final Exception e) {
      fullAddress =
          new FlowAddress(
              address,
              ChainId.builder().chainType(ChainType.flow).chainName(ChainName.mainnet).build());
    }
    return fullAddress;
  }
}
