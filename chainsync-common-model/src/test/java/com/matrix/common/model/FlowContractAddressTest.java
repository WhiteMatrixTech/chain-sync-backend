package com.matrix.common.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author reimia
 */
class FlowContractAddressTest {

  @Test
  void test() {
    final Address address =
        Address.fromFullAddress("testnet_flow-A.7f3812b53dd4de20.MatrixMarketplaceNFT.NFT");
    Assertions.assertEquals(
        "A.7f3812b53dd4de20.MatrixMarketplaceNFT", address.getNormalizedAddress());

    final FlowAddress flowAddress = new FlowAddress("A.7f3812b53dd4de20.MatrixMarketplaceNFT.NFT", ChainId.builder().chainName(ChainName.testnet).chainType(ChainType.flow).build());
    Assertions.assertEquals(
        "A.7f3812b53dd4de20.MatrixMarketplaceNFT", flowAddress.getNormalizedAddress());
  }
}
