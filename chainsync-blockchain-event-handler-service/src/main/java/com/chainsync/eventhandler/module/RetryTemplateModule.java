package com.chainsync.eventhandler.module;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * @author reimia
 */
@Configuration
public class RetryTemplateModule {

  @Bean
  public RetryTemplate retryTemplate() {

    final RetryTemplate retryTemplate = new RetryTemplate();
    FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
    fixedBackOffPolicy.setBackOffPeriod(2000L);
    retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

    final SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(2);
    retryTemplate.setBackOffPolicy(new FixedBackOffPolicy());
    retryTemplate.setRetryPolicy(retryPolicy);

    return retryTemplate;
  }
}
