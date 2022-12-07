package com.chainsync.metric;

import com.chainsync.metric.annotation.CounterMetric;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** @author reimia */
@SpringBootApplication
@RestController
public class TestApplication {
  public static void main(final String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }

  @CounterMetric
  @GetMapping
  void get(@RequestParam final String s) {
    if (!"1".equals(s)) {
      throw new RuntimeException("test");
    }
  }
}
