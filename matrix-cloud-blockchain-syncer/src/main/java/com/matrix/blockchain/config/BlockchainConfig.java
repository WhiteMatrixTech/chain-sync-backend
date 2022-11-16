package com.matrix.blockchain.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author luyuanheng
 */
@Data
@Component
@Configuration
@ConfigurationProperties("blockchain")
public class BlockchainConfig {

  /** ethereum */
  private List<String> ethereumProviderEndpoint;

  /** mumbai */
  private List<String> mumbaiProviderEndpoint;

  /** polygon */
  private List<String> polygonProviderEndpoint;
}
