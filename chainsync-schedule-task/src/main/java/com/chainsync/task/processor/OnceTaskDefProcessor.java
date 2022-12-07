package com.chainsync.task.processor;

import com.chainsync.task.dao.TaskDao;
import com.chainsync.task.executor.MatrixTaskExecutor;
import com.chainsync.task.executor.TaskDefExecutor;
import com.chainsync.task.model.Task;
import com.chainsync.task.model.TaskDef;
import com.chainsync.task.model.TaskRepeatedMode;
import io.micrometer.core.instrument.MeterRegistry;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * @author luyuanheng
 */
@Log4j2
@Component
public class OnceTaskDefProcessor extends RepeatedTaskDefProcessor {

  @Resource TaskDao taskDao;

  @Resource MeterRegistry meterRegistry;

  @Resource RedissonClient redissonClient;

  @Override
  public String process(TaskDef taskDef) {
    for (MatrixTaskExecutor matrixTaskExecutor : matrixTaskExecutors) {
      if (matrixTaskExecutor.isApplicable(taskDef)) {
        Task task =
            new TaskDefExecutor(taskDef, taskDao, matrixTaskExecutor, meterRegistry, redissonClient)
                .execute();
        return task.getTaskId().toString();
      }
    }

    throw new IllegalArgumentException("unsupported task type: " + taskDef.getTaskType());
  }

  @Override
  public boolean isApplicable(TaskDef taskDef) {
    return taskDef != null
        && TaskRepeatedMode.NONE.name().equalsIgnoreCase(taskDef.getRepeatedMode());
  }
}
