package com.chainsync.task.executor;

import com.chainsync.task.model.TaskDef;

/**
 * @author luyuanheng
 */
public interface MatrixTaskExecutor {

  public String execute(TaskDef taskDef);

  public boolean isApplicable(TaskDef taskDef);
}
