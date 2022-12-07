package com.matrix.eventhandler.event;

import com.matrix.eventhandler.BaseSpringTest;
import com.matrix.eventhandler.log.processor.EthLogProcessor;
import com.matrix.eventhandler.model.BlockChainEvent;
import com.matrix.eventhandler.util.BlockchainTestnetLogGenerator;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import javax.annotation.Resource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * @author reimia
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BlockchainEventHandlerManagerIntegrationTest extends BaseSpringTest {

  @Resource EthLogProcessor ethLogProcessor;
  @Resource BlockchainEventHandlerManager blockchainEventHandlerManager;

  private BlockChainEvent phancyPetBlockChainEvent;
  private BlockChainEvent phantaBearBlockChainEvent;
  private HttpServer httpServer;

  @BeforeAll
  void setUp() throws IOException {

    if (ethLogProcessor.canProcess(BlockchainTestnetLogGenerator.getPhantaBearLog())) {
      phantaBearBlockChainEvent =
          ethLogProcessor
              .processBlockchainLog(BlockchainTestnetLogGenerator.getPhantaBearLog())
              .get(0);
    }
    Assertions.assertNotNull(phantaBearBlockChainEvent);

    if (ethLogProcessor.canProcess(BlockchainTestnetLogGenerator.getPhancyPetLog())) {
      phancyPetBlockChainEvent =
          ethLogProcessor
              .processBlockchainLog(BlockchainTestnetLogGenerator.getPhancyPetLog())
              .get(0);
    }
    Assertions.assertNotNull(phancyPetBlockChainEvent);

    httpServer = HttpServer.create(new InetSocketAddress(8081), 0);
    httpServer.createContext(
        "/metadata/api/v1/nft/notifyEthEvent",
        exchange -> {
          System.out.println(IOUtils.toString(exchange.getRequestBody(), Charset.defaultCharset()));
          final byte[] response = "ok".getBytes();
          exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
          exchange.getResponseBody().write(response);
          exchange.close();
        });
    httpServer.start();
  }

  @Test
  void handleProcessedBlockchainEvent_PhantaBear() {
    Assertions.assertDoesNotThrow(
        () ->
            blockchainEventHandlerManager.handleProcessedBlockchainEvent(
                phantaBearBlockChainEvent));
  }

  @Test
  void handleProcessedBlockchainEvent_PhancyPet() {
    Assertions.assertDoesNotThrow(
        () ->
            blockchainEventHandlerManager.handleProcessedBlockchainEvent(phancyPetBlockChainEvent));
  }

  @AfterAll
  void tearDown() {
    httpServer.stop(10);
  }
}
