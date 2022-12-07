package com.matrix.metric;

import io.micrometer.core.instrument.MeterRegistry;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Log4j2
@SpringBootTest(classes = TestApplication.class)
class CounterMetricAspectIntegrationTests {
  @Resource TestApplication application;
  @Resource MeterRegistry registry;

  @Test
  void counter_success_metrics() {
    Assertions.assertDoesNotThrow(
        () -> {
          double count =
              registry
                  .counter(
                      "custom_counter",
                      "function_name",
                      "TestApplication#get",
                      "function_invoke_result",
                      "success",
                      "function_invoke_failed_reason",
                      "")
                  .count();
          Assertions.assertEquals(0, count);
          application.get("1");
          count =
              registry
                  .counter(
                      "custom_counter",
                      "function_name",
                      "TestApplication#get",
                      "function_invoke_result",
                      "success",
                      "function_invoke_failed_reason",
                      "")
                  .count();
          Assertions.assertEquals(1, count);
        });
  }

  @Test
  void counter_failed_metrics() {
    double count =
        registry
            .counter(
                "custom_counter",
                "function_name",
                "TestApplication#get",
                "function_invoke_result",
                "failed",
                "function_invoke_failed_reason",
                "test")
            .count();
    Assertions.assertEquals(0, count);
    Assertions.assertThrows(RuntimeException.class, () -> application.get("2"), "test");
    count =
        registry
            .counter(
                "custom_counter",
                "function_name",
                "TestApplication#get",
                "function_invoke_result",
                "failed",
                "function_invoke_failed_reason",
                "test")
            .count();
    Assertions.assertEquals(1, count);
  }

  // TODO add gauge test
}
