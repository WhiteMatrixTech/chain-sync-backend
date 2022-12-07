package com.matrix.etl.model.response;

import com.matrix.etl.model.Task;
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
public class QueryTaskLogResponse {
  List<Task> tasks;
}
