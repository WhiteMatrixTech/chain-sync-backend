package com.matrix.eventhandler.event.handler;

import com.matrix.eventhandler.abi.AbiEnhancedEventManager;
import com.matrix.eventhandler.event.BlockchainEventHandlerManager;
import com.matrix.eventhandler.log.BlockchainLogProcessorManager;
import com.matrix.eventhandler.log.processor.EthLogProcessor;
import com.matrix.eventhandler.model.BlockChainEvent;
import com.matrix.eventhandler.model.BlockchainEventLogDTO;
import com.matrix.eventhandler.module.DynamoDbModule;
import com.matrix.eventhandler.module.RestTemplateModule;
import com.matrix.eventhandler.util.BlockchainTestnetLogGenerator;
import com.matrix.module.DefaultDynamoModule;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
    webEnvironment = WebEnvironment.NONE,
    classes = {
      BlockchainEventHandlerManager.class,
      PhantaBearEventHandler.class,
      PhantaDogEventHandler.class,
      EthLogProcessor.class,
      AbiEnhancedEventManager.class,
      DynamoDbModule.class,
      DefaultDynamoModule.class,
      RestTemplateModule.class,
      BlockchainLogProcessorManager.class
    })
class PhantaDogEventHandlerIntegrationTest {

  @Autowired PhantaDogEventHandler phantaDogEventHandler;

  @Autowired EthLogProcessor ethLogProcessor;

  /**
   * This test fetches all logs in rinkeby block 10627515, witch contains a phanta pet Transfer
   * event.
   *
   * <p>Step 1: MUST start a matrix-metadata-service at localhost:8081 before test.
   *
   * <p>Step 2: Then modify row (collectionId:'1U4WUkZruHndwCHayg8QoZ', tokenId: 302) in table
   * nft-metadata-local, set its blockNumber to 0 & owner to '0x0'.
   *
   * <p>Step 3: Run this test (integration test)
   *
   * <p>Step 4: Check row (collectionId:'1U4WUkZruHndwCHayg8QoZ', tokenId: 302) in table
   * nft-metadata-local, if its blockNumber == 10627515 && owner ==
   * '0xd001634059C78D1cb7a3A0518E1881E954194127_ethereum', test passed. If not, test failed.
   */
  @Test
  void test() {
    final List<BlockchainEventLogDTO> logs = BlockchainTestnetLogGenerator.phantaPetsBlockLogs();
    Assertions.assertDoesNotThrow(
        () ->
            logs.forEach(
                log -> {
                  if (ethLogProcessor.canProcess(log)) {
                    final BlockChainEvent blockChainEvent =
                        ethLogProcessor.processBlockchainLog(log).get(0);
                    if (phantaDogEventHandler.isApplicable(blockChainEvent)) {
                      phantaDogEventHandler.processBlockChainEvent(blockChainEvent);
                    }
                  }
                }));
  }
}
