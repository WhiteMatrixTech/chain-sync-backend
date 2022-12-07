package com.matrix.task.module;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * @author luyuanheng
 */
@Configuration
@ComponentScan("com.matrix.task.module")
public class TaskScheduleModule {

  private static final String THREAD_NAME_PREFIX = "syncer_task_";

  private static final String SCHEDULER = "scheduler";

  @Bean
  public ScheduledTaskRegistrar scheduledTaskRegistrar() {
    ScheduledTaskRegistrar scheduledTaskRegistrar = new ScheduledTaskRegistrar();
    scheduledTaskRegistrar.setScheduler(threadPoolTaskScheduler());
    return scheduledTaskRegistrar;
  }

  @Bean
  public TaskScheduler threadPoolTaskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(20);
    scheduler.setThreadNamePrefix(THREAD_NAME_PREFIX + SCHEDULER);
    scheduler.initialize();
    return scheduler;
  }
}
