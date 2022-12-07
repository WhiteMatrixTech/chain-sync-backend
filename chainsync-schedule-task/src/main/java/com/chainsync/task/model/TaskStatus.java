package com.chainsync.task.model;

/**
 * @author luyuanheng
 */
public enum TaskStatus {

  /** task is processing */
  PROCESSING,

  /** execute task result is successful */
  SUCCESS,

  /** execute task result failed somewhere */
  FAILURE
}
