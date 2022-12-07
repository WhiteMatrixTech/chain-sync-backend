package com.matrix.eventhandler;

import com.matrix.eventhandler.abi.AbiEnhancedEventManager;
import com.matrix.eventhandler.event.BlockchainEventHandlerManager;
import com.matrix.eventhandler.event.handler.PhantaBearEventHandler;
import com.matrix.eventhandler.event.handler.PhantaDogEventHandler;
import com.matrix.eventhandler.event.handler.TheirsverseTransferEventHandler;
import com.matrix.eventhandler.log.BlockchainLogProcessorManager;
import com.matrix.eventhandler.log.processor.EthLogProcessor;
import com.matrix.eventhandler.module.DynamoDbModule;
import com.matrix.eventhandler.module.RestTemplateModule;
import com.matrix.module.DefaultDynamoModule;
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
