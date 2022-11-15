package com.matrix.task.service.impl;

import com.matrix.task.dao.TaskDefDao;
import com.matrix.task.executor.BlockchainSyncTaskExecutor;
import com.matrix.task.model.TaskDef;
import com.matrix.task.processor.TaskDefProcessor;
import com.matrix.task.service.TaskService;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author luyuanheng
 */
@Log4j2
@Service
public class TaskServiceImpl implements TaskService {

  @Resource TaskDefDao taskDefDao;

  @Resource List<TaskDefProcessor> processors;

  /** refresh task interval ${schedule.taskDelay} */
  @Override
  @Scheduled(fixedDelayString = "${schedule.taskDelay}")
  public void refreshTask() {
    List<TaskDef> taskDefs = taskDefDao.scan();
    taskDefs.stream().forEach(taskDef -> processTask(taskDef));
  }

  /**
   * process task
   *
   * @param taskDef task def
   * @return task id
   */
  @Override
  public String processTask(TaskDef taskDef) {
    for (TaskDefProcessor processor : this.processors) {
      // step 1. choose applicable processor to handle task def
      if (processor.isApplicable(taskDef)) {
        // step 2. clean changed task & create executor & submit to taskScheduler
        return processor.process(taskDef);
      }
    }

    throw new IllegalArgumentException("unsupported repeated mode: " + taskDef.getRepeatedMode());
  }
}
