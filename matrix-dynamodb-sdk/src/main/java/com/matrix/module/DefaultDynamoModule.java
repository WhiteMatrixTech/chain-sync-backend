package com.matrix.module;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author reimia
 */
@Configuration
public class DefaultDynamoModule {

  @Value("${cloud.aws.region.static:ap-northeast-1}")
  private String region;

  @Bean
  @ConditionalOnMissingBean
  public AmazonDynamoDB amazonDynamoDB() {
    return AmazonDynamoDBClientBuilder.standard().withRegion(region).build();
  }

  @Bean
  @ConditionalOnMissingBean
  public DynamoDB dynamoDB(final AmazonDynamoDB amazonDynamoDB) {
    return new DynamoDB(amazonDynamoDB);
  }
}
