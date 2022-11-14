package com.matrix.metric.util;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Gauge.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

/** @author reimia */
@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetricUtil {

  /** maintain the AtomicLong in gauge, change the AtomicLong will change the gauge */
  private static final Map<String, AtomicLong> map = new HashMap<>();

  /** reference: https://micrometer.io/docs/concepts#_gauges */
  public static void addGauge(
      final MeterRegistry registry, final String name, final long value, final Tag... tags) {
    try {
      if (map.get(name) != null) {
        map.get(name).set(value);
        log.info(
            "[MetricUtil] addGauge successfully! name:{},result:{},tags:{}", name, value, tags);
        return;
      }
      final AtomicLong atomicLong = new AtomicLong(value);
      final Builder<AtomicLong> builder = Gauge.builder(name, atomicLong, AtomicLong::get);
      if (tags.length > 0) {
        builder.tags(Arrays.asList(tags));
      }
      final Gauge register = builder.register(registry);
      map.put(name, atomicLong);
      log.info(
          "[MetricUtil] addGauge successfully! name:{},result:{},tags:{}",
          name,
          register.value(),
          tags);
    } catch (final RuntimeException runtimeException) {
      log.warn("[MetricUtil] addGauge failed! name:{},tags:{}", name, tags);
    }
  }
}
