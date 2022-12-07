package com.chainsync.eventhandler.module;

import com.chainsync.eventhandler.listener.BlockchainLogListener;
import com.chainsync.eventhandler.model.BlockchainEventLogDTO;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

/**
 * @author reimia
 */
@Log4j2
@Configuration
public class KafkaModule {

  @SneakyThrows
  @Bean
  public KafkaMessageListenerContainer<String, BlockchainEventLogDTO>
  getEthereumBlockchainLogListenerContainer(
      @Value("${topics.ethereum-event-log-topic.name}") final String topicName,
      final KafkaProperties kafkaProperties,
      final BlockchainLogListener blockchainLogListener) {

    return this.getBlockchainLogListenerContainer(
        topicName, kafkaProperties, blockchainLogListener);
  }

  @Bean
  public KafkaMessageListenerContainer<String, BlockchainEventLogDTO>
  getPolygonBlockchainLogListenerContainer(
      @Value("${topics.polygon-event-log-topic.name}") final String topicName,
      final KafkaProperties kafkaProperties,
      final BlockchainLogListener blockchainLogListener) {

    return this.getBlockchainLogListenerContainer(
        topicName, kafkaProperties, blockchainLogListener);
  }

  @Bean
  public KafkaMessageListenerContainer<String, BlockchainEventLogDTO>
  getRinkebyBlockchainLogListenerContainer(
      @Value("${topics.rinkeby-event-log-topic.name}") final String topicName,
      final KafkaProperties kafkaProperties,
      final BlockchainLogListener blockchainLogListener) {

    return this.getBlockchainLogListenerContainer(
        topicName, kafkaProperties, blockchainLogListener);
  }

  @Bean
  public KafkaMessageListenerContainer<String, BlockchainEventLogDTO>
  getMumbaiBlockchainLogListenerContainer(
      @Value("${topics.mumbai-event-log-topic.name}") final String topicName,
      final KafkaProperties kafkaProperties,
      final BlockchainLogListener blockchainLogListener) {

    return this.getBlockchainLogListenerContainer(
        topicName, kafkaProperties, blockchainLogListener);
  }

  @SneakyThrows
  private KafkaMessageListenerContainer<String, BlockchainEventLogDTO>
  getBlockchainLogListenerContainer(
      final String topicName,
      final KafkaProperties kafkaProperties,
      final BlockchainLogListener blockchainLogListener) {

    final ContainerProperties containerProperties = new ContainerProperties(topicName);
    containerProperties.setMessageListener(blockchainLogListener);
    containerProperties.setGroupId(kafkaProperties.getConsumer().getGroupId());

    final Map<String, Object> configProps = kafkaProperties.buildConsumerProperties();

    final ConsumerFactory<String, BlockchainEventLogDTO> consumerFactory =
        new DefaultKafkaConsumerFactory<>(configProps);
    final KafkaMessageListenerContainer<String, BlockchainEventLogDTO> listenerContainer =
        new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
    listenerContainer.setAutoStartup(true);

    // bean name is the prefix of kafka consumer thread name
    listenerContainer.setBeanName("BlockchainLog-listener");
    return listenerContainer;
  }
}
