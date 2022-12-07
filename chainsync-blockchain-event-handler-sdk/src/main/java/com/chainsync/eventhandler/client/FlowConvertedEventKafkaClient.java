package com.chainsync.eventhandler.client;

import com.chainsync.eventhandler.model.FlowConvertedEventsDTO;
import lombok.Value;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * @author reimia
 */
@Value
public class FlowConvertedEventKafkaClient {
  KafkaTemplate<String, FlowConvertedEventsDTO> kafkaClient;
  NewTopic defaultTopic;

  public ListenableFuture<SendResult<String, FlowConvertedEventsDTO>> sendDefault(
      final String key, final FlowConvertedEventsDTO message) {
    return this.kafkaClient.send(this.defaultTopic.name(), key, message);
  }
}
