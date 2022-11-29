package com.matrix.etl.model.response;

import com.matrix.etl.model.SimpleApp;
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
public class QueryAppResponse {
  List<SimpleApp> apps;
}