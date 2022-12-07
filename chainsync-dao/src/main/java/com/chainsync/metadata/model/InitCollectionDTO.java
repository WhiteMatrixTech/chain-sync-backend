package com.chainsync.metadata.model;

import com.chainsync.common.model.Address;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * @author yangjian
 * @date 2022/2/15
 */
@Value
@Builder
public class InitCollectionDTO {
  @NonNull String appId;
  @NonNull Address contractAddress;
  @NonNull String templateId;
  @NonNull String deploymentTxHash;
  @NonNull String walletId;
  String collectionName;
  String description;
}
