package com.chainsync.task;

import com.chainsync.common.model.PackageConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties
@SpringBootApplication(
    scanBasePackages = {
      PackageConstants.SCHEDULE_TASK_PACKAGE,
      PackageConstants.COMMON_PACKAGE,
      PackageConstants.BASE_SCAN_PACKAGES,
      PackageConstants.METRICS_SDK_PACKAGE
    })
@EnableAsync
@EnableScheduling
public class TaskApplication {

  public static void main(String[] args) {
    SpringApplication.run(TaskApplication.class, args);
  }
}
