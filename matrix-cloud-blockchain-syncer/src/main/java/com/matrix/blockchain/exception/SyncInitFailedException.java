package com.matrix.blockchain.exception;

import static com.matrix.blockchain.constants.Constants.NULL_BLOCK;

/**
 * @author shuyizhang
 */
public class SyncInitFailedException extends SyncException {

  public SyncInitFailedException() {
    super(NULL_BLOCK);
  }

  public SyncInitFailedException(final String message) {
    super(NULL_BLOCK, message);
  }

  public SyncInitFailedException(final String message, final Throwable cause) {
    super(NULL_BLOCK, message, cause);
  }
}
