package com.chainsync.common.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** @author reimia */
class EthereumAddressTest {

  @Test
  void EthereumAddressCompare() {
    EthereumAddress ethereumAddress1 =
        new EthereumAddress("0x7c6Be4c5fBC6239094Ff92E3ec6c6808A965b06E", ChainId.builder().chainName(ChainName.rinkeby).build());
    EthereumAddress ethereumAddress2 =
        new EthereumAddress("0x7c6be4c5fbc6239094ff92e3ec6c6808a965b06e", ChainId.builder().chainName(ChainName.rinkeby).build());
    EthereumAddress ethereumAddress3 =
        new EthereumAddress("7c6Be4c5fBC6239094Ff92E3ec6c6808A965b06E", ChainId.builder().chainName(ChainName.rinkeby).build());
    EthereumAddress ethereumAddress4 =
        new EthereumAddress("7c6be4c5fbc6239094ff92e3ec6c6808a965b06e", ChainId.builder().chainName(ChainName.rinkeby).build());
    Assertions.assertEquals(ethereumAddress1, ethereumAddress2);
    Assertions.assertEquals(ethereumAddress2, ethereumAddress3);
    Assertions.assertEquals(ethereumAddress3, ethereumAddress4);
  }

  @Test
  void testTrimming() {
    EthereumAddress ethereumAddress1 = new EthereumAddress("0x0", ChainId.builder().chainName(ChainName.rinkeby).build());
    EthereumAddress ethereumAddress2 = new EthereumAddress("0x00", ChainId.builder().chainName(ChainName.rinkeby).build());
    System.out.println(ethereumAddress1);
    Assertions.assertEquals(ethereumAddress1, ethereumAddress2);
  }
}
