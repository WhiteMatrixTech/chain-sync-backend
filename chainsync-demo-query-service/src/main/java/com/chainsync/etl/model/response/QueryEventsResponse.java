package com.chainsync.etl.model.response;

import com.chainsync.etl.model.EthereumBlockEvent;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * @author richard
 */
@Builder
@Jacksonized
@Value
public class QueryEventsResponse {
  List<EthereumBlockEvent> events;
}
