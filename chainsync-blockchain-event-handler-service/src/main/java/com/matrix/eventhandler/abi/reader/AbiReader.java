package com.matrix.eventhandler.abi.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.matrix.common.model.AbiEntry;
import com.matrix.common.model.AbiEntryType;
import com.matrix.common.model.AbiEvent;
import com.matrix.eventhandler.model.AbiEnhancedEvent;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * @author shuyizhang
 */
@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AbiReader {

  private static final ObjectMapper objectMapper =
      new ObjectMapper().configure(SerializationFeature.WRAP_ROOT_VALUE, true);

  @SneakyThrows
  public static List<AbiEnhancedEvent> readEvents(final String abiJsonString) {
    return readEvents(abiJsonString, null);
  }

  @SneakyThrows
  public static List<AbiEnhancedEvent> readEvents(
      final String abiJsonString, final Set<String> filter) {
    final AbiEntry[] abiEntries = objectMapper.readValue(abiJsonString, AbiEntry[].class);
    return Stream.of(abiEntries)
        .filter(abiEntry -> abiEntry.getType().equals(AbiEntryType.EVENT))
        .filter(
            abiEntry -> {
              if (filter == null) {
                return true;
              }
              return filter.contains(abiEntry.getName());
            })
        .map(AbiEvent.class::cast)
        .map(AbiEnhancedEvent::new)
        .collect(Collectors.toList());
  }
}
