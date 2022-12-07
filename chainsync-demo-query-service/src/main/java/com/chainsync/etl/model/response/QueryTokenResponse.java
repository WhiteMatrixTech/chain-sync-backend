package com.chainsync.etl.model.response;

import com.chainsync.etl.model.Token;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * @author reimia
 */
@Builder
@Jacksonized
@Value
public class QueryTokenResponse {

  List<Token> tokens;
}
