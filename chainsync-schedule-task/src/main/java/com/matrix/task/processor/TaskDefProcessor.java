package com.matrix.task.processor;

import com.matrix.task.model.TaskDef;

/**
 * @author luyuanheng
 */
public interface TaskDefProcessor {

  public String process(TaskDef taskDef);

  public boolean isApplicable(TaskDef taskDef);

}
