package com.matrix.eventhandler.module;

import com.matrix.eventhandler.client.BlockchainLogKafkaClient;
import com.matrix.eventhandler.client.BlockchainTransactionHistoryKafkaClient;
import com.matrix.eventhandler.client.BlockchainTransactionKafkaClient;
import com.matrix.eventhandler.client.FlowConvertedEventHistoryKafkaClient;
import com.matrix.eventhandler.client.FlowConvertedEventKafkaClient;
import com.matrix.eventhandler.client.MetadataRefreshMessageKafkaClient;
import com.matrix.eventhandler.model.BlockchainEventLogDTO;
import com.matrix.eventhandler.model.BlockchainTransactionDTO;
import com.matrix.eventhandler.model.FlowConvertedEventsDTO;
import com.matrix.eventhandler.model.MetadataRefreshMessageDTO;
import com.matrix.eventhandler.util.YamlPropertySourceFactory;
import lombok.SneakyThrows;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * @author reimia
 */
@Lazy
@Configuration
@EnableKafka
@PropertySource(
    value = {
        "classpath:matrix-cloud-blockchain-event-handler-config-${STAGE:local}.yml",
        "classpath:matrix-cloud-common-kafka-${STAGE:local}.yml"
    },
    factory = YamlPropertySourceFactory.class)
public class EventHandlerKafkaClientModule {

  @Bean("rinkebyEventLogTopic")
  public NewTopic getRinkebyEventLogTopic(
      @Value("${topics.rinkeby-event-log-topic.name}") final String topicName,
      @Value("${topics.rinkeby-event-log-topic.replicas}") final int topicReplicas,
      @Value("${topics.rinkeby-event-log-topic.partitions}") final int topicPartitions) {
    return TopicBuilder.name(topicName).partitions(topicPartitions).replicas(topicReplicas).build();
  }

  @Bean("ethereumEventLogTopic")
  public NewTopic getEthereumEventLogTopic(
      @Value("${topics.ethereum-event-log-topic.name}") final String topicName,
      @Value("${topics.ethereum-event-log-topic.replicas}") final int topicReplicas,
      @Value("${topics.ethereum-event-log-topic.partitions}") final int topicPartitions) {
    return TopicBuilder.name(topicName).partitions(topicPartitions).replicas(topicReplicas).build();
  }

  @Bean("mumbaiEventLogTopic")
  public NewTopic getMumbaiEventLogTopic(
      @Value("${topics.mumbai-event-log-topic.name}") final String topicName,
      @Value("${topics.mumbai-event-log-topic.replicas}") final int topicReplicas,
      @Value("${topics.mumbai-event-log-topic.partitions}") final int topicPartitions) {
    return TopicBuilder.name(topicName).partitions(topicPartitions).replicas(topicReplicas).build();
  }

  @Bean("polygonEventLogTopic")
  public NewTopic getPolygonEventLogTopic(
      @Value("${topics.polygon-event-log-topic.name}") final String topicName,
      @Value("${topics.polygon-event-log-topic.replicas}") final int topicReplicas,
      @Value("${topics.polygon-event-log-topic.partitions}") final int topicPartitions) {
    return TopicBuilder.name(topicName).partitions(topicPartitions).replicas(topicReplicas).build();
  }

  @Bean("flowTestNetEventLogTopic")
  public NewTopic getFlowEventLogTestNetTopic(
      @Value("${topics.flow-test-net-event-log-topic.name}") final String topicName,
      @Value("${topics.flow-test-net-event-log-topic.replicas}") final int topicReplicas,
      @Value("${topics.flow-test-net-event-log-topic.partitions}") final int topicPartitions) {
    return TopicBuilder.name(topicName).partitions(topicPartitions).replicas(topicReplicas).build();
  }

  @Bean("flowMainNetEventLogTopic")
  public NewTopic getFlowEventLogMainNetTopic(
      @Value("${topics.flow-main-net-event-log-topic.name}") final String topicName,
      @Value("${topics.flow-main-net-event-log-topic.replicas}") final int topicReplicas,
      @Value("${topics.flow-main-net-event-log-topic.partitions}") final int topicPartitions) {
    return TopicBuilder.name(topicName).partitions(topicPartitions).replicas(topicReplicas).build();
  }

  @SneakyThrows
  @Bean("flowTestNetBlockchainLogKafkaClient")
  public BlockchainLogKafkaClient getFlowBlockchainLogClientTestNetProvider(
      final KafkaTemplate<String, BlockchainEventLogDTO> template,
      @Qualifier("flowTestNetEventLogTopic") final NewTopic newTopic) {
    template.setDefaultTopic(newTopic.name());
    return new BlockchainLogKafkaClient(template, newTopic);
  }

  @SneakyThrows
  @Bean("flowMainNetBlockchainLogKafkaClient")
  public BlockchainLogKafkaClient getFlowBlockchainLogClientMainNetProvider(
      final KafkaTemplate<String, BlockchainEventLogDTO> template,
      @Qualifier("flowMainNetEventLogTopic") final NewTopic newTopic) {
    template.setDefaultTopic(newTopic.name());
    return new BlockchainLogKafkaClient(template, newTopic);
  }

  @SneakyThrows
  @Bean("rinkebyBlockchainLogKafkaClient")
  public BlockchainLogKafkaClient getRinkebyBlockchainLogClientProvider(
      final KafkaTemplate<String, BlockchainEventLogDTO> template,
      @Qualifier("rinkebyEventLogTopic") final NewTopic newTopic) {
    template.setDefaultTopic(newTopic.name());
    return new BlockchainLogKafkaClient(template, newTopic);
  }

  @SneakyThrows
  @Bean("ethereumBlockchainLogKafkaClient")
  public BlockchainLogKafkaClient getEthereumBlockchainLogClientProvider(
      final KafkaTemplate<String, BlockchainEventLogDTO> template,
      @Qualifier("ethereumEventLogTopic") final NewTopic newTopic) {
    template.setDefaultTopic(newTopic.name());
    return new BlockchainLogKafkaClient(template, newTopic);
  }

  @SneakyThrows
  @Bean("mumbaiBlockchainLogKafkaClient")
  public BlockchainLogKafkaClient getMumbaiBlockchainLogClientProvider(
      final KafkaTemplate<String, BlockchainEventLogDTO> template,
      @Qualifier("mumbaiEventLogTopic") final NewTopic newTopic) {
    template.setDefaultTopic(newTopic.name());
    return new BlockchainLogKafkaClient(template, newTopic);
  }

  @SneakyThrows
  @Bean("polygonBlockchainLogKafkaClient")
  public BlockchainLogKafkaClient getPolygonBlockchainLogClientProvider(
      final KafkaTemplate<String, BlockchainEventLogDTO> template,
      @Qualifier("polygonEventLogTopic") final NewTopic newTopic) {
    template.setDefaultTopic(newTopic.name());
    return new BlockchainLogKafkaClient(template, newTopic);
  }

  @Bean("flowTestNetTransactionTopic")
  public NewTopic getFlowTestNetTransactionTopic(
      @Value("${topics.flow-test-net-transaction-topic.name}") final String topicName,
      @Value("${topics.flow-test-net-transaction-topic.replicas}") final int topicReplicas,
      @Value("${topics.flow-test-net-transaction-topic.partitions}") final int topicPartitions,
      @Value("${topics.flow-test-net-transaction-topic.max.request.size}") final String maxRequestSize) {
    return TopicBuilder.name(topicName)
        .partitions(topicPartitions)
        .replicas(topicReplicas)
        .config(TopicConfig.MAX_MESSAGE_BYTES_CONFIG, maxRequestSize)
        .build();
  }

  @Bean("flowMainNetTransactionTopic")
  public NewTopic getFlowMainNetTransactionTopic(
      @Value("${topics.flow-main-net-transaction-topic.name}") final String topicName,
      @Value("${topics.flow-main-net-transaction-topic.replicas}") final int topicReplicas,
      @Value("${topics.flow-main-net-transaction-topic.partitions}") final int topicPartitions,
      @Value("${topics.flow-main-net-transaction-topic.max.request.size}") final String maxRequestSize) {
    return TopicBuilder.name(topicName)
        .partitions(topicPartitions)
        .replicas(topicReplicas)
        .config(TopicConfig.MAX_MESSAGE_BYTES_CONFIG, maxRequestSize)
        .build();
  }

  @SneakyThrows
  @Bean("flowTestNetTransactionKafkaClient")
  public BlockchainTransactionKafkaClient getFlowTestNetTransactionClientProvider(
      final KafkaTemplate<String, BlockchainTransactionDTO> template,
      @Qualifier("flowTestNetTransactionTopic") final NewTopic newTopic) {
    return new BlockchainTransactionKafkaClient(template, newTopic);
  }

  @SneakyThrows
  @Bean("flowMainNetTransactionKafkaClient")
  public BlockchainTransactionKafkaClient getFlowMainNetTransactionClientProvider(
      final KafkaTemplate<String, BlockchainTransactionDTO> template,
      @Qualifier("flowMainNetTransactionTopic") final NewTopic newTopic) {
    return new BlockchainTransactionKafkaClient(template, newTopic);
  }

  @Bean("flowMainNetTransactionHistoryTopic")
  public NewTopic getFlowMainNetTransactionHistoryTopic(
      @Value("${topics.flow-main-net-transaction-history-topic.name}") final String topicName,
      @Value("${topics.flow-main-net-transaction-history-topic.replicas}") final int topicReplicas,
      @Value("${topics.flow-main-net-transaction-history-topic.partitions}") final int topicPartitions,
      @Value("${topics.flow-main-net-transaction-history-topic.max.request.size}") final String maxRequestSize) {
    return TopicBuilder.name(topicName)
        .partitions(topicPartitions)
        .replicas(topicReplicas)
        .config(TopicConfig.MAX_MESSAGE_BYTES_CONFIG, maxRequestSize)
        .build();
  }

  @SneakyThrows
  @Bean("flowMainNetTransactionHistoryKafkaClient")
  public BlockchainTransactionHistoryKafkaClient getFlowMainNetTransactionHistoryClientProvider(
      final KafkaTemplate<String, BlockchainTransactionDTO> template,
      @Qualifier("flowMainNetTransactionHistoryTopic") final NewTopic newTopic) {
    return new BlockchainTransactionHistoryKafkaClient(template, newTopic);
  }

  @Bean("flowConvertedEventsTopic")
  public NewTopic getFlowConvertedEventsTopic(
      @Value("${topics.flow-converted-events-topic.name}") final String topicName,
      @Value("${topics.flow-converted-events-topic.replicas}") final int topicReplicas,
      @Value("${topics.flow-converted-events-topic.partitions}") final int topicPartitions) {
    return TopicBuilder.name(topicName).partitions(topicPartitions).replicas(topicReplicas).build();
  }

  @SneakyThrows
  @Bean("flowConvertedEventsKafkaClient")
  public FlowConvertedEventKafkaClient getFlowConvertedEventsClientProvider(
      final KafkaTemplate<String, FlowConvertedEventsDTO> template,
      @Qualifier("flowConvertedEventsTopic") final NewTopic newTopic) {
    return new FlowConvertedEventKafkaClient(template, newTopic);
  }

  @Bean("flowConvertedEventsHistoryTopic")
  public NewTopic getFlowConvertedEventsHistoryTopic(
      @Value("${topics.flow-converted-events-history-topic.name}") final String topicName,
      @Value("${topics.flow-converted-events-history-topic.replicas}") final int topicReplicas,
      @Value("${topics.flow-converted-events-history-topic.partitions}") final int topicPartitions) {
    return TopicBuilder.name(topicName).partitions(topicPartitions).replicas(topicReplicas).build();
  }

  @SneakyThrows
  @Bean("flowConvertedEventsHistoryKafkaClient")
  public FlowConvertedEventHistoryKafkaClient getFlowConvertedEventsHistoryClientProvider(
      final KafkaTemplate<String, FlowConvertedEventsDTO> template,
      @Qualifier("flowConvertedEventsHistoryTopic") final NewTopic newTopic) {
    return new FlowConvertedEventHistoryKafkaClient(template, newTopic);
  }
}
