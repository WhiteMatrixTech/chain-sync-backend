package com.chainsync.blockchain.exception;

import com.chainsync.blockchain.constants.Constants;

/**
 * @author shuyizhang
 */
public class SyncInitFailedException extends SyncException {

  public SyncInitFailedException() {
    super(Constants.NULL_BLOCK);
  }

  public SyncInitFailedException(final String message) {
    super(Constants.NULL_BLOCK, message);
  }

  public SyncInitFailedException(final String message, final Throwable cause) {
    super(Constants.NULL_BLOCK, message, cause);
  }
}
