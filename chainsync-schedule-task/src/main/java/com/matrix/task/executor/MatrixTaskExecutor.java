package com.matrix.task.executor;

import com.matrix.task.model.TaskDef;

/**
 * @author luyuanheng
 */
public interface MatrixTaskExecutor {

  public String execute(TaskDef taskDef);

  public boolean isApplicable(TaskDef taskDef);
}
