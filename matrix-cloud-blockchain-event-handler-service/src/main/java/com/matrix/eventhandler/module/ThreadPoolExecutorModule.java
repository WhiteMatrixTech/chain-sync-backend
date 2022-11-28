package com.matrix.eventhandler.module;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author reimia
 */
@Configuration
@EnableAsync
public class ThreadPoolExecutorModule {

  public static final String CACHED_EXECUTOR = "cachedExecutor";

  @Bean(name = CACHED_EXECUTOR)
  public ThreadPoolExecutor getCachedAsyncRestExecutor() {
    final BasicThreadFactory factory =
        new BasicThreadFactory.Builder()
            .namingPattern("async-cached-%d")
            .daemon(true)
            .priority(Thread.MAX_PRIORITY)
            .build();

    return new ThreadPoolExecutor(
        2, 80, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), factory);
  }

}
