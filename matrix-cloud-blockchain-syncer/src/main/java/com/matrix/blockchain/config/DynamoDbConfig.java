package com.matrix.blockchain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author luyuanheng
 */
@Data
@Component
@ConfigurationProperties(prefix = "dynamodb")
public class DynamoDbConfig {
  private String rinkebyEventTableName;
  private String mumbaiEventTableName;
  private String ethereumEventTableName;
  private String polygonEventTableName;
  private String blockchainTipTableName;
  private String syncErrorTableName;
  private String blockchainOffsetTableName;
  private String blockSuccessTableName;
  private String blockFailedTableName;
}
