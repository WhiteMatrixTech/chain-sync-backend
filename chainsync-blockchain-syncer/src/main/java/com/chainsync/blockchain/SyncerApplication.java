package com.chainsync.blockchain;

import com.chainsync.common.model.PackageConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author luyuanheng
 */
@SpringBootApplication(
    scanBasePackages = {
      PackageConstants.EVENT_HANDLER_PACKAGE,
      PackageConstants.COMMON_PACKAGE,
      PackageConstants.METRICS_SDK_PACKAGE,
      PackageConstants.BLOCKCHAIN_SERVICE_PACKAGE
    })
@EnableConfigurationProperties
public class SyncerApplication {

  public static void main(String[] args) {
    SpringApplication.run(SyncerApplication.class, args);
  }
}
