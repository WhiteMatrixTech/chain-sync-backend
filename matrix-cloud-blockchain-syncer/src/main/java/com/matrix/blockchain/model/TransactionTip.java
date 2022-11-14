package com.matrix.blockchain.model;

import com.matrix.dynamodb.annotation.DynamoAttribute;
import com.matrix.dynamodb.annotation.DynamoKey;
import com.matrix.dynamodb.annotation.DynamoTable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transaction tip for transaction syncer.
 *
 * @author ShenYang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoTable
public class TransactionTip {
  public static final String ATTR_TASK_ID = "taskId";
  public static final String ATTR_TIP = "tip";
  public static final String ATTR_TIME = "time";

  /** Task id. */
  @DynamoKey
  @DynamoAttribute(attributeName = ATTR_TASK_ID)
  private String taskId;

  /** Ledger version. */
  @DynamoAttribute(attributeName = ATTR_TIP)
  private Long tip;

  /** Tip time. */
  @DynamoAttribute(attributeName = ATTR_TIME)
  private Instant time;
}
