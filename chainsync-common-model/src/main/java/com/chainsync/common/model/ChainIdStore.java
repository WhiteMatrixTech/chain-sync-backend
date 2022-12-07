package com.chainsync.common.model;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author reimia
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChainIdStore {

  /** may store in ddb */
  public static final List<ChainId> CHAIN_IDS =
      List.of(
          new ChainId(ChainType.ethereum, ChainName.mainnet, 1L),
          new ChainId(ChainType.ethereum, ChainName.rinkeby, 4L),
          new ChainId(ChainType.polygon, ChainName.mainnet, 137L),
          new ChainId(ChainType.polygon, ChainName.mumbai, 80001L),
          new ChainId(ChainType.flow, ChainName.mainnet, null),
          new ChainId(ChainType.flow, ChainName.testnet, null));

  public static List<ChainName> getAvailableChainId(final ChainType chainType) {
    return CHAIN_IDS.stream()
        .filter(chainId -> chainId.getChainType().equals(chainType))
        .map(ChainId::getChainName)
        .collect(Collectors.toList());
  }

  public static long getChainIdNumber(final ChainType chainType, final ChainName chainName) {
    return getChainId(chainType, chainName).getChainIdNumber();
  }

  public static ChainId getChainId(final ChainType chainType, final ChainName chainName) {
    final List<ChainId> collect =
        CHAIN_IDS.stream()
            .filter(
                chainId ->
                    chainId.getChainType().equals(chainType)
                        && chainId.getChainName().equals(chainName))
            .collect(Collectors.toList());
    if (collect.isEmpty()) {
      throw new IllegalStateException("illegal match with ChainType and ChainId");
    }
    return collect.get(0);
  }

  public static ChainId getChainId(final ChainType chainType) {
    final List<ChainId> collect =
        CHAIN_IDS.stream()
            .filter(
                chainId ->
                    !chainId.getChainName().equals(ChainName.mainnet)
                        && chainId.getChainType().equals(chainType))
            .collect(Collectors.toList());
    return collect.get(0);
  }
}
