package com.matrix.task.service;

import com.matrix.task.model.TaskDef;

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
