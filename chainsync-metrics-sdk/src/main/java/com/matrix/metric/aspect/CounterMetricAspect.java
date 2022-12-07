package com.matrix.metric.aspect;

import com.matrix.metric.annotation.CounterMetric;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Log4j2
@Component
@AllArgsConstructor
public class CounterMetricAspect {

  private static final String COUNTER_FUNCTION_NAME = "custom_counter";
  private static final String TAG_FUNCTION_NAME = "function_name";
  private static final String TAG_FUNCTION_INVOKE_RESULT = "function_invoke_result";
  private static final String TAG_FUNCTION_INVOKE_FAILED_REASON = "function_invoke_failed_reason";

  private final MeterRegistry registry;

  @Around(value = "@within(counterMetric) || @annotation(counterMetric)")
  public Object exposeMetrics(
      final ProceedingJoinPoint proceedingJoinPoint, final CounterMetric counterMetric)
      throws Throwable {
    try {
      final Object result = proceedingJoinPoint.proceed();
      registerSuccessMetrics(proceedingJoinPoint);
      return result;
    } catch (final Throwable err) {
      registerFailedMetrics(proceedingJoinPoint, err);
      throw err;
    }
  }

  private void registerSuccessMetrics(final ProceedingJoinPoint proceedingJoinPoint) {
    try {
      Counter.builder(COUNTER_FUNCTION_NAME)
          .tags(
              TAG_FUNCTION_NAME,
              proceedingJoinPoint.getSignature().getDeclaringType().getSimpleName()
                  + "#"
                  + proceedingJoinPoint.getSignature().getName(),
              TAG_FUNCTION_INVOKE_RESULT,
              "success",
              TAG_FUNCTION_INVOKE_FAILED_REASON,
              "")
          .register(registry)
          .increment();
      log.debug("Report Metrics Success");
    } catch (final Throwable t) {
      log.warn("Report Metrics Error:{}", t.getMessage());
    }
  }

  private void registerFailedMetrics(
      final ProceedingJoinPoint proceedingJoinPoint, final Throwable err) {
    try {
      Counter.builder(COUNTER_FUNCTION_NAME)
          .tags(
              TAG_FUNCTION_NAME,
              proceedingJoinPoint.getSignature().getDeclaringType().getSimpleName()
                  + "#"
                  + proceedingJoinPoint.getSignature().getName(),
              TAG_FUNCTION_INVOKE_RESULT,
              "failed",
              TAG_FUNCTION_INVOKE_FAILED_REASON,
              err.getMessage())
          .register(registry)
          .increment();
      log.debug("Report Metrics Success");
    } catch (final Throwable t) {
      log.warn("Report Metrics Error: {}", t.getMessage());
    }
  }
}
