package com.matrix.blockchain.module;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author luyuanheng
 */
@Configuration
public class S3BeanModule {

  @Value("${spring.profiles.active:local}")
  private String profile;

  @Value("${cloud.aws.region.static}")
  private String region;

  @Bean
  @Primary
  public AmazonS3 getS3Client() {
    if (!"local".equals(profile)) {
      return AmazonS3ClientBuilder.standard().withRegion(region).build();
    } else {
      return AmazonS3ClientBuilder.standard().withEndpointConfiguration(
          new AwsClientBuilder.EndpointConfiguration("http://localhost:9999", region)).build();
    }
  }

  @Bean
  public TransferManager getTransferManager(final AmazonS3 s3Client) {
    return TransferManagerBuilder.standard().withS3Client(s3Client).build();
  }
}
