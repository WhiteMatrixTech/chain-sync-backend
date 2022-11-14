package com.matrix.blockchain.service.impl;

import com.matrix.blockchain.dao.BlockOffsetDao;
import com.matrix.blockchain.model.BlockOffset;
import com.matrix.blockchain.model.GetOffsetRequest;
import com.matrix.blockchain.model.GetTransactionEventsRequest;
import com.matrix.blockchain.model.GetTransactionEventsResponse;
import com.matrix.blockchain.processor.EventProcessor;
import com.matrix.blockchain.service.BlockService;
import com.matrix.common.model.ChainId;
import com.matrix.common.model.ChainName;
import com.matrix.common.model.ChainType;
import com.matrix.metric.util.MetricUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * @author luyuanheng
 */
@Log4j2
@Service
public class BlockServiceImpl implements BlockService {

  @Resource List<EventProcessor> eventProcessors;

  @Resource BlockOffsetDao blockOffsetDao;

  @Resource MeterRegistry meterRegistry;

  /**
   * get transaction events by transactionHash
   *
   * @param request
   * @return events list
   */
  @Override
  public GetTransactionEventsResponse getTransactionEvents(GetTransactionEventsRequest request) {
    for (EventProcessor eventProcessor : eventProcessors) {
      if (eventProcessor.isApplicable(request.getChainType())) {
        return eventProcessor.getTransactionEvents(request);
      }
    }

    throw new IllegalArgumentException(
        String.format("not support chainType: %s", request.getChainType()));
  }

  /**
   * get offset
   *
   * @param request
   * @return chainId offset
   */
  @Override
  public Long getOffset(GetOffsetRequest request) {
    ChainId chainId =
        ChainId.builder()
            .chainType(ChainType.valueOf(request.getChainType()))
            .chainName(ChainName.valueOf(request.getChainName()))
            .build();
    BlockOffset offset = blockOffsetDao.getItem(chainId.toString(), request.getStart());
    if (offset != null) {
      if (request.getReport()) {
        MetricUtil.addGauge(
            meterRegistry,
            "blockchain_offset",
            offset.getOffset(),
            new Tag[] {Tag.of("chainId", chainId.toString())});
      }

      return offset.getOffset();
    }

    return null;
  }
}
