package com.matrix.etl.model.response;

import com.matrix.etl.model.SimpleTransaction;
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
