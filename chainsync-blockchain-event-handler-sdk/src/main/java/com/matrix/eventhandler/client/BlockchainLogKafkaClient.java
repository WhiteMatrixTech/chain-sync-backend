package com.matrix.eventhandler.client;

import com.matrix.eventhandler.model.BlockchainEventLogDTO;
import lombok.Value;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * @author reimia
 */
@Value
public class BlockchainLogKafkaClient {
  KafkaTemplate<String, BlockchainEventLogDTO> kafkaClient;
  NewTopic defaultTopic;

  public ListenableFuture<SendResult<String, BlockchainEventLogDTO>> sendDefault(
      final String key, final BlockchainEventLogDTO message) {
    return this.kafkaClient.send(this.defaultTopic.name(), key, message);
  }
}
