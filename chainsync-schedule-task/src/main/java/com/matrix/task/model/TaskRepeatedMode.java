package com.matrix.task.model;

/**
 * @author luyuanheng
 */
public enum TaskRepeatedMode {

  /** no repeated */
  NONE,

  /** repeated as cron */
  CRON,

  /** repeated as fixed delay */
  FIXED_DELAY,

  /** repeated as fixed rate */
  FIXED_RATE
}
