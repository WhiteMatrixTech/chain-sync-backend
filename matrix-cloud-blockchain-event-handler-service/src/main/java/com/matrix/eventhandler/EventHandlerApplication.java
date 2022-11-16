package com.matrix.eventhandler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * @author reimia
 */
@EnableKafka
@SpringBootApplication
public class EventHandlerApplication {

  public static void main(final String[] args) {
    SpringApplication.run(EventHandlerApplication.class, args);
  }
}
