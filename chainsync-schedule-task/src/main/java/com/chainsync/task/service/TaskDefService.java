package com.chainsync.task.service;

import com.chainsync.task.model.TaskDef;

/**
 * @author luyuanheng
 */
public interface TaskDefService {

  /**
   * create or update task def
   *
   * @param taskDef task def
   * @return task name
   */
  String upsertTaskDef(TaskDef taskDef);

  /**
   * delete task
   *
   * @param taskName task name
   * @return task id
   */
  String deleteTaskDef(String taskName);
}
