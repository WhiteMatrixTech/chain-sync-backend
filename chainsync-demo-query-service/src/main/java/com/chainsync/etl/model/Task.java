package com.chainsync.etl.model;

import com.chainsync.dynamodb.annotation.DynamoAttribute;
import com.chainsync.dynamodb.annotation.DynamoKey;
import com.chainsync.dynamodb.annotation.DynamoTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author luyuanheng
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoTable
public class Task {

  public static final String ATTR_TASK_ID = "taskId";
  public static final String ATTR_TASK_NAME = "taskName";
  public static final String ATTR_CREATE_TIME = "createTime";
  public static final String ATTR_LAST_EXECUTE_TIME = "lastExecuteTime";
  public static final String ATTR_STATUS = "status";
  public static final String ATTR_ERROR_INFO = "errorInfo";

  @DynamoKey
  @DynamoAttribute(attributeName = ATTR_TASK_NAME)
  private String taskName;

  @DynamoKey(dynamoKeyType = DynamoKey.RANGE)
  @DynamoAttribute(attributeName = ATTR_TASK_ID, attributeType = "N")
  private Long taskId;

  @DynamoAttribute(attributeName = ATTR_CREATE_TIME, attributeType = "N")
  private Long createTime;

  @DynamoAttribute(attributeName = ATTR_LAST_EXECUTE_TIME, attributeType = "N")
  private Long lastExecuteTime;

  @DynamoAttribute(attributeName = ATTR_STATUS)
  private String status;

  @DynamoAttribute(attributeName = ATTR_ERROR_INFO)
  private String errorInfo;
}
