package com.chainsync.task.service.impl;

import com.google.common.base.Preconditions;
import com.chainsync.task.dao.TaskDefDao;
import com.chainsync.task.model.TaskDef;
import com.chainsync.task.model.TaskRepeatedMode;
import com.chainsync.task.service.TaskDefService;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author luyuanheng
 */
@Log4j2
@Service
public class TaskDefServiceImpl implements TaskDefService {

  @Resource TaskDefDao taskDefDao;

  /**
   * create or update task def
   *
   * @param taskDef task def
   * @return task name
   */
  @Override
  public String upsertTaskDef(TaskDef taskDef) {
    String taskName = taskDef.getTaskName();
    Preconditions.checkArgument(StringUtils.isNotBlank(taskName), "task name is empty");
    Preconditions.checkArgument(
        StringUtils.isNotBlank(taskDef.getTaskType()),
        "task type is empty, should be a callable interface name");
    Preconditions.checkArgument(
        TaskRepeatedMode.NONE.name().equalsIgnoreCase(taskDef.getRepeatedMode())
            || StringUtils.isNotBlank(taskDef.getExpression()),
        "repeated mode: " + taskDef.getRepeatedMode() + " is not none, expression can't be empty");

    Long current = System.currentTimeMillis();
    taskDef.setLatestModifyTime(current);
    TaskDef def = taskDefDao.getItem(taskName);
    if (def != null) {
      if (!taskDef.equals(def)) {
        taskDefDao.updateTaskDef(taskDef);
      }
    } else {
      // create task def
      taskDef.setCreateTime(current);
      taskDefDao.putItem(taskDef);
    }

    return taskDef.getTaskName();
  }

  /**
   * delete task
   *
   * @param taskName task name
   * @return task id
   */
  @Override
  public String deleteTaskDef(String taskName) {
    TaskDef taskDef = taskDefDao.deleteItem(taskName);
    return taskDef == null ? null : taskDef.getTaskName();
  }
}
