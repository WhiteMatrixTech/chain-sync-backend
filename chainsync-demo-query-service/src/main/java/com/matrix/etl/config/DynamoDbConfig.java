package com.matrix.etl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author richard
 */
@Data
@Component
@ConfigurationProperties(prefix = "dynamodb")
public class DynamoDbConfig {
  private String taskDefTableName;
  private String taskTableName;
  private String tokenTableName;
  private String ethTransactionTableName;
  private String ethereumEventTableName;
  private String blockchainTipTableName;
}
