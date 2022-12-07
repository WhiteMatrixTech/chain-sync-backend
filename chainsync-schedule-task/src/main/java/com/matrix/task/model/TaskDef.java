package com.matrix.task.model;

import com.matrix.dynamodb.annotation.DynamoAttribute;
import com.matrix.dynamodb.annotation.DynamoKey;
import com.matrix.dynamodb.annotation.DynamoTable;
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
public class TaskDef {

  public static final String ATTR_TASK_NAME = "taskName";
  public static final String ATTR_TASK_TYPE = "taskType";
  public static final String ATTR_PARAMS = "params";
  public static final String ATTR_REPEATED_MODE = "repeated_mode";
  public static final String ATTR_EXPRESSION = "expression";
  public static final String ATTR_SYNC = "sync";
  public static final String ATTR_EXECUTE_MOST_FOR = "executeMostFor";
  public static final String ATTR_CREATE_TIME = "createTime";
  public static final String ATTR_LATEST_MODIFY_TIME = "latestModifyTime";
  public static final String ATTR_DELETE = "delete";

  @DynamoKey
  @DynamoAttribute(attributeName = ATTR_TASK_NAME)
  private String taskName;

  @DynamoAttribute(attributeName = ATTR_TASK_TYPE)
  private String taskType;

  @Builder.Default
  @DynamoAttribute(attributeName = ATTR_PARAMS)
  private String params = "{}";

  @Builder.Default
  @DynamoAttribute(attributeName = ATTR_REPEATED_MODE)
  private String repeatedMode = TaskRepeatedMode.NONE.name();

  @DynamoAttribute(attributeName = ATTR_EXPRESSION)
  private String expression;

  @Builder.Default
  @DynamoAttribute(attributeName = ATTR_SYNC, attributeType = "BOOL")
  private Boolean sync = true;

  @DynamoAttribute(attributeName = ATTR_LATEST_MODIFY_TIME, attributeType = "N")
  private Long latestModifyTime;

  @DynamoAttribute(attributeName = ATTR_CREATE_TIME, attributeType = "N")
  private Long createTime;

  @Builder.Default
  @DynamoAttribute(attributeName = ATTR_DELETE, attributeType = "BOOL")
  private Boolean delete = false;
}
