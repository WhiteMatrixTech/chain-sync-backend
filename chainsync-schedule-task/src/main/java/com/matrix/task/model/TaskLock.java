package com.matrix.task.model;

import com.matrix.dynamodb.annotation.DynamoAttribute;
import com.matrix.dynamodb.annotation.DynamoKey;
import com.matrix.dynamodb.annotation.DynamoTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author luyuanheng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoTable
public class TaskLock {

  public static final String ATTR_ID = "_id";
  public static final String ATTR_LOCKED_AT = "lockedAt";
  public static final String ATTR_LOCKED_BY = "lockedBy";
  public static final String ATTR_LOCK_UNTIL = "lockUntil";

  @DynamoKey
  @DynamoAttribute(attributeName = ATTR_ID)
  private String _id;

  @DynamoAttribute(attributeName = ATTR_LOCKED_AT)
  private String lockedAt;

  @DynamoAttribute(attributeName = ATTR_LOCKED_BY)
  private String lockedBy;

  @DynamoAttribute(attributeName = ATTR_LOCK_UNTIL)
  private String lockUntil;
}
