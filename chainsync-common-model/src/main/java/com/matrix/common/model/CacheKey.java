package com.matrix.common.model;

/**
 * @author luyuanheng
 */
public class CacheKey {

  private static final String COLLECTION_OWNER_COUNT_PREFIX = "marketplace:collection:owner_count:";
  private static final String COLLECTION_OWNERS_PREFIX = "OWNERS_";

  public static String getCollectionOwnerCountKey(String contractAddress) {
    return COLLECTION_OWNER_COUNT_PREFIX + contractAddress;
  }

  public static String getCollectionOwnersKey(String contractAddress) {
    return COLLECTION_OWNERS_PREFIX + contractAddress;
  }
}
