package com.chainsync.task.config;

import javax.annotation.Resource;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Redisson config
 *
 * @author luyuanheng
 */
@Component
public class RedissonConfig {

  @Resource RedisProperties redisProperties;

  /**
   * Redisson client
   *
   * @return redisson client
   */
  @Bean
  public RedissonClient redissonClient() {
    final Config config = new Config();
    config
        .useSingleServer()
        .setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort())
        .setDatabase(redisProperties.getDatabase());
    if (StringUtils.hasText(redisProperties.getPassword())) {
      config.useSingleServer().setPassword(redisProperties.getPassword());
    }
    return Redisson.create(config);
  }
}
