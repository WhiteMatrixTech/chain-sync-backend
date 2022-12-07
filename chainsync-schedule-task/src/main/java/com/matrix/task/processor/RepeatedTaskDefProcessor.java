package com.matrix.task.processor;

import com.matrix.task.dao.TaskDao;
import com.matrix.task.executor.MatrixTaskExecutor;
import com.matrix.task.executor.TaskDefExecutor;
import com.matrix.task.model.TaskDef;
import com.matrix.task.model.TaskRepeatedMode;
import io.micrometer.core.instrument.MeterRegistry;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RedissonClient;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.stereotype.Component;

/**
 * @author luyuanheng
 */
@Log4j2
@Component
public class RepeatedTaskDefProcessor implements TaskDefProcessor {

  protected static final Map<String, Long> TASK_MODIFY_TIME_MAP = new HashMap<String, Long>();
  protected static final Map<String, ScheduledTask> TASK_MAP = new HashMap<String, ScheduledTask>();

  @Resource TaskDao taskDao;

  @Resource List<MatrixTaskExecutor> matrixTaskExecutors;

  @Resource ScheduledTaskRegistrar scheduledTaskRegistrar;

  @Resource MeterRegistry meterRegistry;

  @Resource RedissonClient redissonClient;

  @SneakyThrows
  public Runnable getProcessor(TaskDef taskDef) {
    // get execute method
    Method execute = ReflectUtils.findDeclaredMethod(TaskDefExecutor.class, "execute", null);

    log.info(
        "config executor success, taskName: {}, wrapper it into ScheduledMethodRunnable.",
        taskDef.getTaskName());
    for (MatrixTaskExecutor matrixTaskExecutor : matrixTaskExecutors) {
      if (matrixTaskExecutor.isApplicable(taskDef)) {
        return new ScheduledMethodRunnable(
            new TaskDefExecutor(
                taskDef, taskDao, matrixTaskExecutor, meterRegistry, redissonClient),
            execute);
      }
    }

    throw new IllegalArgumentException("unsupported task type: " + taskDef.getTaskType());
  }

  @Override
  public String process(TaskDef taskDef) {
    String taskName = taskDef.getTaskName();
    if (taskDef.getDelete()) {
      if (TASK_MAP.get(taskName) != null) {
        // release task lock
        releaseLock(taskDef);
      }
    } else {
      if (!TASK_MODIFY_TIME_MAP.containsKey(taskName)
          || TASK_MODIFY_TIME_MAP.get(taskName).longValue()
              != taskDef.getLatestModifyTime().longValue()) {
        if (TASK_MODIFY_TIME_MAP.containsKey(taskName) && TASK_MAP.containsKey(taskName)) {
          // task def has be changed
          // release task lock
          releaseLock(taskDef);
        }

        // create executor & submit to task scheduler
        ScheduledTask scheduledTask = getScheduleTask(taskDef);

        // cache task
        TASK_MODIFY_TIME_MAP.put(taskName, taskDef.getLatestModifyTime());
        TASK_MAP.put(taskName, scheduledTask);
      }
    }

    return taskDef.getTaskName();
  }

  @Override
  public boolean isApplicable(TaskDef taskDef) {
    return taskDef != null
        && (TaskRepeatedMode.CRON.name().equalsIgnoreCase(taskDef.getRepeatedMode())
            || TaskRepeatedMode.FIXED_DELAY.name().equalsIgnoreCase(taskDef.getRepeatedMode())
            || TaskRepeatedMode.FIXED_RATE.name().equalsIgnoreCase(taskDef.getRepeatedMode()));
  }

  private ScheduledTask getScheduleTask(TaskDef taskDef) {
    if (TaskRepeatedMode.CRON.name().equalsIgnoreCase(taskDef.getRepeatedMode())) {
      return scheduledTaskRegistrar.scheduleCronTask(
          new CronTask(getProcessor(taskDef), taskDef.getExpression()));
    } else if (TaskRepeatedMode.FIXED_DELAY.name().equalsIgnoreCase(taskDef.getRepeatedMode())) {
      return scheduledTaskRegistrar.scheduleFixedDelayTask(
          new FixedDelayTask(getProcessor(taskDef), Long.valueOf(taskDef.getExpression()), 0));
    } else if (TaskRepeatedMode.FIXED_RATE.name().equalsIgnoreCase(taskDef.getRepeatedMode())) {
      return scheduledTaskRegistrar.scheduleFixedRateTask(
          new FixedRateTask(getProcessor(taskDef), Long.valueOf(taskDef.getExpression()), 0));
    }

    throw new IllegalArgumentException(
        String.format("unsupport repeated mode: %s", taskDef.getRepeatedMode()));
  }

  private void releaseLock(TaskDef taskDef) {
    String taskName = taskDef.getTaskName();
    TASK_MAP.get(taskName).cancel();
    log.info("cancel task: {} success", taskName);

    TASK_MAP.remove(taskName);
    TASK_MODIFY_TIME_MAP.remove(taskName);
    log.info("release task lock: {} success", taskName);
  }
}
