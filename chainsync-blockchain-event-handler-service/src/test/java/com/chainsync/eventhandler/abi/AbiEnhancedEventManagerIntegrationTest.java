package com.chainsync.eventhandler.abi;

import com.chainsync.eventhandler.BaseSpringTest;
import com.chainsync.eventhandler.model.AbiEnhancedEvent;
import com.chainsync.common.model.Address;
import java.util.List;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * this test test abi in ddb
 *
 * @author reimia
 */
class AbiEnhancedEventManagerIntegrationTest extends BaseSpringTest {

  @Resource AbiEnhancedEventManager abiEnhancedEventManager;

  @Test
  void getPhantaBearAbiEvent() {
    final List<AbiEnhancedEvent> abiEvent =
        abiEnhancedEventManager.getAbiEvent(
            Address.fromFullAddress("rinkeby_ethereum-0xA9D29DB8B0A2e87482B7D2EaD732aaf161dAC3D9"));
    abiEvent.forEach(
        abiEnhancedEvent ->
            System.out.println(abiEnhancedEvent.getName() + ":" + abiEnhancedEvent.getEventHash()));
    // should be below
    // AdminChanged:0x7e644d79422f17c01e4894b5f4f588d331ebfa28653d42ae832dc59e38c9798f
    // Approval:0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925
    // ApprovalForAll:0x17307eab39ab6107e8899845ad3d59bd9653f200f220920489ca2b5937696c31
    // BeaconUpgraded:0x1cf3b03a6cf19fa2baba4df148e9dcabedea7f8a5c07840e207e5c089be95d3e
    // Initialized:0x7f26b83ff96e1f2b6a682f133852f6798a09c465da95921460cefb3847402498
    // OwnershipTransferred:0x8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e0
    // Paused:0x62e78cea01bee320cd4e420270b5ea74000d11b0c9f74754ebdbfc544b05a258
    // RoleAdminChanged:0xbd79b86ffe0ab8e8776151514217cd7cacd52c909f66475c3af44e129f0b00ff
    // RoleGranted:0x2f8788117e7eff1d82e926ec794901d17c78024a50270940304540a733656f0d
    // RoleRevoked:0xf6391f5c32d9c69d2a47ea670b442974b53935d1edc7fd64eb21e047a839171b
    // TokenMinted:0x96234cb3d6c373a1aaa06497a540bc166d4b0359243a088eaf95e21d7253d0be
    // TokenPriceChanged:0x23c6ec2e2c4752cf5eafabbd0ae9246dce6d5c78f1ed2fde615826e084eee068
    // Transfer:0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef
    // Unpaused:0x5db9ee0a495bf2e6ff9c91a7834c1ba4fdd244a5e8aa4e537bd38aeae4b073aa
    // Upgraded:0xbc7cd75a20ee27fd9adebab32041f755214dbc6bffa90cc0225b39da2e5c2d3b
    Assertions.assertFalse(abiEvent.isEmpty());
  }

  @Test
  void getPhancyPetAbiEvent() {
    final List<AbiEnhancedEvent> abiEvent =
        abiEnhancedEventManager.getAbiEvent(
            Address.fromFullAddress("rinkeby_ethereum-0x554478E4c47EF61806fab268d1B74543d4D01f91"));
    abiEvent.forEach(
        abiEnhancedEvent ->
            System.out.println(abiEnhancedEvent.getName() + ":" + abiEnhancedEvent.getEventHash()));
    // should be below
    // Approval:0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925
    // ApprovalForAll:0x17307eab39ab6107e8899845ad3d59bd9653f200f220920489ca2b5937696c31
    // OwnershipTransferred:0x8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e0
    // Paused:0x62e78cea01bee320cd4e420270b5ea74000d11b0c9f74754ebdbfc544b05a258
    // RoleAdminChanged:0xbd79b86ffe0ab8e8776151514217cd7cacd52c909f66475c3af44e129f0b00ff
    // RoleGranted:0x2f8788117e7eff1d82e926ec794901d17c78024a50270940304540a733656f0d
    // RoleRevoked:0xf6391f5c32d9c69d2a47ea670b442974b53935d1edc7fd64eb21e047a839171b
    // Transfer:0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef
    // Unpaused:0x5db9ee0a495bf2e6ff9c91a7834c1ba4fdd244a5e8aa4e537bd38aeae4b073aa
    Assertions.assertFalse(abiEvent.isEmpty());
  }

  @Test
  void getTheirsverseAbiEvent() {
    final List<AbiEnhancedEvent> abiEvent =
        abiEnhancedEventManager.getAbiEvent(
            Address.fromFullAddress("rinkeby_ethereum-0x98BBeEc683452BA7FEa3BFEc6e0774fC87d5B5C0"));
    abiEvent.forEach(
        abiEnhancedEvent ->
            System.out.println(abiEnhancedEvent.getName() + ":" + abiEnhancedEvent.getEventHash()));
    // should be below
    // Approval:0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925
    // ApprovalForAll:0x17307eab39ab6107e8899845ad3d59bd9653f200f220920489ca2b5937696c31
    // OwnershipTransferred:0x8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e0
    // Paused:0x62e78cea01bee320cd4e420270b5ea74000d11b0c9f74754ebdbfc544b05a258
    // RoleAdminChanged:0xbd79b86ffe0ab8e8776151514217cd7cacd52c909f66475c3af44e129f0b00ff
    // RoleGranted:0x2f8788117e7eff1d82e926ec794901d17c78024a50270940304540a733656f0d
    // RoleRevoked:0xf6391f5c32d9c69d2a47ea670b442974b53935d1edc7fd64eb21e047a839171b
    // Transfer:0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef
    // Unpaused:0x5db9ee0a495bf2e6ff9c91a7834c1ba4fdd244a5e8aa4e537bd38aeae4b073aa
    Assertions.assertFalse(abiEvent.isEmpty());
  }
}
