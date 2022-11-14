package com.matrix.blockchain.module;

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

  @Value("${cloud.aws.region.static}")
  private String region;

  @Bean
  @Primary
  public AmazonS3 getS3Client() {
    return AmazonS3ClientBuilder.standard().withRegion(region).build();
  }

  @Bean
  public TransferManager getTransferManager(final AmazonS3 s3Client) {
    return TransferManagerBuilder.standard().withS3Client(s3Client).build();
  }
}
