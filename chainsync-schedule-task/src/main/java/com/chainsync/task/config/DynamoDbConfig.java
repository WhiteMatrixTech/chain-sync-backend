package com.chainsync.task.config;

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
  private String taskDefTableName;
  private String taskTableName;
}
