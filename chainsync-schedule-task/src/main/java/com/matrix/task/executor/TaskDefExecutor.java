package com.matrix.task.executor;

import com.matrix.metric.util.MetricUtil;
import com.matrix.task.dao.TaskDao;
import com.matrix.task.model.Task;
import com.matrix.task.model.TaskDef;
import com.matrix.task.model.TaskStatus;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@Log4j2
public class TaskDefExecutor {

  private final TaskDef taskDef;

  private final TaskDao taskDao;

  private final MatrixTaskExecutor matrixTaskExecutor;

  private final MeterRegistry meterRegistry;

  private final RedissonClient redissonClient;

  private static LongAdder ERROR_COUNT = new LongAdder();

  public TaskDefExecutor(
      TaskDef taskDef,
      TaskDao taskDao,
      MatrixTaskExecutor matrixTaskExecutor,
      MeterRegistry meterRegistry,
      RedissonClient redissonClient) {
    this.taskDef = taskDef;
    this.taskDao = taskDao;
    this.matrixTaskExecutor = matrixTaskExecutor;
    this.meterRegistry = meterRegistry;
    this.redissonClient = redissonClient;
  }

  public Task execute() {
    final RLock lock = this.redissonClient.getLock(taskDef.getTaskName());
    boolean getLock = false;
    try {
      getLock = lock.tryLock(10, 30000, TimeUnit.MILLISECONDS);
      if (getLock) {
        Task task =
            Task.builder()
                .taskId(System.nanoTime())
                .taskName(taskDef.getTaskName())
                .createTime(System.currentTimeMillis())
                .status(TaskStatus.PROCESSING.name())
                .build();
        // add task log
        taskDao.putItem(task);
        log.info(
            "create task success, taskId: {}, taskName: {}", task.getTaskId(), task.getTaskName());

        // invoker method
        if (taskDef.getSync()) {
          executeSync(task, taskDef, matrixTaskExecutor);
        } else {
          throw new UnsupportedOperationException("current not support async task");
        }

        return task;
      } else {
        log.info("execute task get lock failed, taskDef: {}", taskDef);
      }
    } catch (final Exception e) {
      log.error("execute task get lock failed: {}", e);
    } finally {
      if (getLock) {
        lock.unlock();
      }
    }
    return null;
  }

  public void executeSync(Task task, TaskDef taskDef, MatrixTaskExecutor matrixTaskExecutor) {
    try {
      log.info("start execute taskDef: {}, task: {}", taskDef, task);
      long start = System.currentTimeMillis();

      matrixTaskExecutor.execute(taskDef);

      long cost = System.currentTimeMillis() - start;
      log.info("end execute taskDef: {}, task: {}, cost: {} mills", taskDef, task, cost);

      taskDao.deleteItem(task.getTaskName(), task.getTaskId());
    } catch (Exception e) {
      log.error("execute task: {} error: {}", task, e);
      ERROR_COUNT.increment();
      MetricUtil.addGauge(
          meterRegistry,
          "task_execute_error",
          ERROR_COUNT.longValue(),
          new ImmutableTag("taskName", taskDef.getTaskName()));

      task.setStatus(TaskStatus.FAILURE.name());
      task.setErrorInfo(e.getMessage());
      task.setLastExecuteTime(System.currentTimeMillis());
      // update task status
      taskDao.updateTask(task);
    }
  }
}
