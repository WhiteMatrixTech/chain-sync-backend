package com.chainsync.eventhandler;

import com.chainsync.eventhandler.abi.AbiEnhancedEventManager;
import com.chainsync.eventhandler.event.BlockchainEventHandlerManager;
import com.chainsync.eventhandler.event.handler.PhantaBearEventHandler;
import com.chainsync.eventhandler.event.handler.PhantaDogEventHandler;
import com.chainsync.eventhandler.event.handler.TheirsverseTransferEventHandler;
import com.chainsync.eventhandler.log.BlockchainLogProcessorManager;
import com.chainsync.eventhandler.log.processor.EthLogProcessor;
import com.chainsync.eventhandler.module.DynamoDbModule;
import com.chainsync.eventhandler.module.RestTemplateModule;
import com.chainsync.module.DefaultDynamoModule;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author reimia
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = {
      BlockchainEventHandlerManager.class,
      PhantaBearEventHandler.class,
      PhantaDogEventHandler.class,
      EthLogProcessor.class,
      AbiEnhancedEventManager.class,
      DynamoDbModule.class,
      DefaultDynamoModule.class,
      RestTemplateModule.class,
      BlockchainLogProcessorManager.class,
      TheirsverseTransferEventHandler.class
    })
public class BaseSpringTest {}
