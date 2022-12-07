package com.chainsync.etl.model.response;

import com.chainsync.etl.model.SimpleTransaction;
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
public class QueryTransactionResponse {
  List<SimpleTransaction> transactions;
}
