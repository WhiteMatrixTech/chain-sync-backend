package com.chainsync.task.processor;

import com.chainsync.task.model.TaskDef;

/**
 * @author luyuanheng
 */
public interface TaskDefProcessor {

  public String process(TaskDef taskDef);

  public boolean isApplicable(TaskDef taskDef);

}
