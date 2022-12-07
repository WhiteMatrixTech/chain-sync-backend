package com.matrix.eventhandler.model;

import com.matrix.common.model.AbiEvent;
import com.matrix.common.model.AbiParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;

/**
 * @author shuyizhang
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class AbiEnhancedEvent extends Event {

  @EqualsAndHashCode.Include private final String eventHash;
  private final List<String> parameterNames;
  private final AbiEvent eventMetadata;

  public AbiEnhancedEvent(final AbiEvent eventMetadata) {
    super(
        eventMetadata.getName(),
        eventMetadata.getInputs().stream()
            .map(AbiParam::toWeb3AbiType)
            .collect(Collectors.toList()));
    this.parameterNames =
        eventMetadata.getInputs().stream().map(AbiParam::getName).collect(Collectors.toList());
    this.eventMetadata = eventMetadata;
    this.eventHash = EventEncoder.encode(this);
  }

  public boolean isLogMatched(final String log) {
    return log.equals(this.getEventHash());
  }

  private boolean isLogMatched(final List<String> logs) {
    return logs.get(0).equals(this.getEventHash());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Map<String, Type<?>> decodeToMap(final List<String> logs, final String data) {
    if (this.isLogMatched(logs)) {
      final List<AbiParam> unindexedParams = new ArrayList<>();
      final Map<String, Type<?>> map = new HashMap<>();
      IntStream.range(0, this.getEventMetadata().getInputs().size())
          .forEach(
              idx -> {
                final AbiParam input = this.getEventMetadata().getInputs().get(idx);
                if (input.getIndexed()) {
                  final Type<?> indexedValue =
                      FunctionReturnDecoder.decodeIndexedValue(
                          logs.get(idx + 1), input.toWeb3AbiType());
                  map.put(input.getName(), indexedValue);
                } else {
                  unindexedParams.add(input);
                }
              });

      if (!unindexedParams.isEmpty()) {
        final List<Type> unindexedTypes =
            FunctionReturnDecoder.decode(
                data,
                unindexedParams.stream()
                    .map(abiParam -> (TypeReference<Type>) abiParam.toWeb3AbiType())
                    .collect(Collectors.toList()));

        IntStream.range(0, unindexedParams.size())
            .forEach(i -> map.put(unindexedParams.get(i).getName(), unindexedTypes.get(i)));
      }
      return map;
    }
    throw new IllegalArgumentException("Log does not match event hash");
  }
}
