package com.matrix.eventhandler.log;

import com.matrix.common.model.ChainType;
import com.matrix.eventhandler.BaseSpringTest;
import com.matrix.eventhandler.model.BlockchainEventLogDTO;
import com.matrix.eventhandler.util.BlockchainMainnetLogGenerator;
import com.matrix.eventhandler.util.BlockchainTestnetLogGenerator;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
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
class BlockchainLogProcessorManagerIntegrationTest extends BaseSpringTest {

  @Resource BlockchainLogProcessorManager blockchainLogProcessorManager;

  private HttpServer httpServer;

  @BeforeAll
  void setUp() throws IOException {
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
  void handleBlockchainLog_PhantaBear() {
    Assertions.assertDoesNotThrow(
        () ->
            blockchainLogProcessorManager.handleBlockchainLog(
                ChainType.ethereum, BlockchainTestnetLogGenerator.getPhantaBearLog()));
  }

  @Test
  void handleBlockchainLog_PhancyPet() {
    Assertions.assertDoesNotThrow(
        () ->
            blockchainLogProcessorManager.handleBlockchainLog(
                ChainType.ethereum, BlockchainTestnetLogGenerator.getPhancyPetLog()));
  }

  @Test
  void handleBlockchainLog_Theirsverse() {
    Assertions.assertDoesNotThrow(
        () -> {
          final List<BlockchainEventLogDTO> blockchainEventLogDTOS =
              BlockchainTestnetLogGenerator.ThiersverseBlockLogs();
          blockchainEventLogDTOS.forEach(
              blockchainEventLogDTO ->
                  blockchainLogProcessorManager.handleBlockchainLog(
                      ChainType.ethereum, blockchainEventLogDTO));
        });
  }

  @Test
  void handleBlockchainLog_Theirsverse_multi() {
    Assertions.assertDoesNotThrow(
        () -> {
          final List<BlockchainEventLogDTO> blockchainEventLogDTOS =
              BlockchainMainnetLogGenerator.theirsverseMultiLogs();
          blockchainEventLogDTOS.forEach(
              blockchainEventLogDTO ->
                  blockchainLogProcessorManager.handleBlockchainLog(
                      ChainType.ethereum, blockchainEventLogDTO));
        });
  }

  @AfterAll
  void tearDown() {
    httpServer.stop(10);
  }
}
