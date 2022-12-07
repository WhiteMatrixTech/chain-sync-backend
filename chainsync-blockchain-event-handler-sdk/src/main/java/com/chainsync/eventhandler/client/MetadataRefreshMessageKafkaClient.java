package com.chainsync.eventhandler.client;

import com.chainsync.eventhandler.model.MetadataRefreshMessageDTO;
import lombok.Value;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * @author richard
 */
@Value
public class MetadataRefreshMessageKafkaClient {
  KafkaTemplate<String, MetadataRefreshMessageDTO> kafkaClient;
  NewTopic defaultTopic;

  public ListenableFuture<SendResult<String, MetadataRefreshMessageDTO>> sendDefault(
      final String key, final MetadataRefreshMessageDTO message) {
    return this.kafkaClient.send(this.defaultTopic.name(), key, message);
  }
}
