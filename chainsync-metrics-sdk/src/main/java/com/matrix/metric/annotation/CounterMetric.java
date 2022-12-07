package com.matrix.metric.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * this annotation will create an aop around function and record if the function throw exception to
 * metrics
 *
 * @author reimia
 */
@Retention(value = RetentionPolicy.RUNTIME)
public @interface CounterMetric {
  boolean alertOnFailed() default false;
}
