package com.chainsync.eventhandler.client;

import com.chainsync.eventhandler.model.BlockchainTransactionDTO;
import lombok.Value;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * @author luyuanheng
 */
@Value
public class BlockchainTransactionHistoryKafkaClient {
  KafkaTemplate<String, BlockchainTransactionDTO> kafkaClient;
  NewTopic defaultTopic;

  public ListenableFuture<SendResult<String, BlockchainTransactionDTO>> sendDefault(
      final String key, final BlockchainTransactionDTO message) {
    return this.kafkaClient.send(this.defaultTopic.name(), key, message);
  }
}
