package com.chainsync.blockchain.module;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author luyuanheng
 */
@Configuration
public class GsonModule {

  @Bean
  public Gson getGson() {
    return new Gson();
  }
}
