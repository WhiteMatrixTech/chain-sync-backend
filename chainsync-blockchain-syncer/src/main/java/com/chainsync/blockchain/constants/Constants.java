package com.chainsync.blockchain.constants;

/**
 * @author luyuanheng
 */
public class Constants {

  public static final int ERROR_STRING_MAX_WIDTH = 1024;

  public static final long NULL_BLOCK = 0L;

  public static final String CONNECTOR = ":";

  public static final int BLOCK_MAX_RANGE = 2000;

  // ddb max size is 400kb, data max size config 300kb
  public static final int DATA_MAX_SIZE = 300 * 1024;

  public static final String EVENT_FOLDER = "/event";

  public static final String FILE_PATH_SEPARATOR = "/";

  public static final int BATCH_WRITE_MAX = 25;
}
