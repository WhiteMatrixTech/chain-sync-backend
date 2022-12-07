package com.chainsync.task.service;

import com.chainsync.task.model.TaskDef;

/**
 * @author luyuanheng
 */
public interface TaskService {

  /**
   * refresh task
   */
  void refreshTask();

  /**
   * process task
   * @param taskDef task def
   * @return task id
   */
  String processTask(TaskDef taskDef);

}
