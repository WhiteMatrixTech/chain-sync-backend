package com.chainsync.task.model;

/**
 * @author luyuanheng
 */
public enum TaskType {
  SYNC_BLOCKCHAIN,
  SYNC_REQUEST,
  SYNC_RETRY_REQUEST,
  SYNC_RETRY_TASK,
  OFFSET,
  INFAMOUS_POLLING_STAKING_TIME,
  SYNC_TRANSACTION
}
