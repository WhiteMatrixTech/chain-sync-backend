package com.matrix.eventhandler.listener;

import com.matrix.common.model.ChainType;
import com.matrix.eventhandler.log.BlockchainLogProcessorManager;
import com.matrix.eventhandler.model.BlockchainEventLogDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

/**
 * @author reimia
 */
@Log4j2
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BlockchainLogListener implements MessageListener<String, BlockchainEventLogDTO> {

  private final BlockchainLogProcessorManager blockchainLogProcessorManager;

  @Override
  public void onMessage(final ConsumerRecord<String, BlockchainEventLogDTO> data) {
    final BlockchainEventLogDTO rawEventAvro = data.value();
    log.debug("[BlockchainLogListener.onMessage] receive BlockchainLogDTO : {}", rawEventAvro);
    // TODO use Executor
    blockchainLogProcessorManager.handleBlockchainLog(
        ChainType.valueOf(rawEventAvro.getChainType()), rawEventAvro);
  }
}
