package com.chainsync.common.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author reimia
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PackageConstants {

  public static final String BASE_SCAN_PACKAGES = "com.chainsync.module";

  public static final String BLOCKCHAIN_SERVICE_PACKAGE = "com.chainsync.blockchain";

  public static final String METRICS_SDK_PACKAGE = "com.chainsync.metrics";

  public static final String COMMON_PACKAGE = "com.chainsync.common";

  public static final String SCHEDULE_TASK_PACKAGE = "com.chainsync.task";

  public static final String EVENT_HANDLER_PACKAGE = "com.chainsync.eventhandler";
}
