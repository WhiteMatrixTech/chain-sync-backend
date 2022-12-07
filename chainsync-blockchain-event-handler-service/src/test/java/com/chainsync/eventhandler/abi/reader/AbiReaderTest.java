package com.chainsync.eventhandler.abi.reader;

import com.chainsync.eventhandler.model.AbiEnhancedEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * @author reimia
 */
@Log4j2
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AbiReaderTest {

  @Test
  void readPhantaBearEvents() throws Exception {
    final String jsonString = this.readFileAsString("phantaBear.json");
    final List<AbiEnhancedEvent> abiEnhancedEvents = AbiReader.readEvents(jsonString, null);
    log.info("events size: {}", abiEnhancedEvents.size());
  }

  @Test
  void readOneSyncEvents() throws Exception {
    final String jsonString = this.readFileAsString("OneSyncERC721_v4.0.0-solidity.json");
    final List<AbiEnhancedEvent> abiEnhancedEvents = AbiReader.readEvents(jsonString, null);
    log.info("events size: {}", abiEnhancedEvents.size());
  }

  private String readFileAsString(final String filePath) throws Exception {
    return Files.readString(
        Path.of(
            Objects.requireNonNull(this.getClass().getClassLoader().getResource(filePath))
                .toURI()));
  }
}
