package com.matrix.blockchain.retriever;

import com.matrix.blockchain.constants.EthError;
import com.matrix.blockchain.exception.SyncInitFailedException;
import com.matrix.blockchain.model.BlockRange;
import com.matrix.blockchain.model.Web3jContainer;
import java.io.SyncFailedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthLog.LogObject;

/**
 * @author luyuanheng
 */
@Log4j2
@Getter
@Component
public class EvmEventRetriever {

  public long getBlockHeight(final Web3jContainer web3jContainer) {
    try {
      return this.getBlockHeightInternal(web3jContainer);
    } catch (final Exception e) {
      log.error("[getBlockHeight] Error: ", e);
      throw new SyncInitFailedException(e.getMessage(), e);
    }
  }

  public List<LogObject> retrieveEvents(
      final Web3jContainer web3jContainer, final BlockRange blockRange) {
    try {
      return this.retrieveEventsInternal(
          web3jContainer,
          new EthFilter(
              new DefaultBlockParameterNumber(blockRange.getFrom()),
              new DefaultBlockParameterNumber(blockRange.getTo()),
              Collections.emptyList()));
    } catch (final Exception e) {
      log.error("[retrieveEvents] Error: ", e);
      throw new SyncInitFailedException(e.getMessage(), e);
    }
  }

  public Map<Long, Block> retrieveBlocks(
      final Web3jContainer web3jContainer, final BlockRange blockRange) {
    try {
      List<DefaultBlockParameterNumber> blockParameterNumbers = new ArrayList<>();
      for (long i = blockRange.getFrom(); i <= blockRange.getTo(); i++) {
        blockParameterNumbers.add(new DefaultBlockParameterNumber(i));
      }
      return this.retrieveBlocksInternal(web3jContainer, blockParameterNumbers);
    } catch (final Exception e) {
      log.error("[retrieveBlocks] Error: ", e);
      throw new SyncInitFailedException(e.getMessage(), e);
    }
  }

  @SneakyThrows
  private List<LogObject> retrieveEventsInternal(
      final Web3jContainer web3jContainer, final EthFilter ethFilter) {

    final EthLog ethLog = web3jContainer.getWeb3j().ethGetLogs(ethFilter).send();
    log.info(
        "ethFilter from: {} to: {}, ethLogs: {}, error: {}",
        ethFilter.getFromBlock().getValue(),
        ethFilter.getToBlock().getValue(),
        ethLog.getLogs() == null ? 0 : ethLog.getLogs().size(),
        ethLog.getError());

    Long from =
        ((DefaultBlockParameterNumber) ethFilter.getFromBlock()).getBlockNumber().longValue();
    Long to = ((DefaultBlockParameterNumber) ethFilter.getToBlock()).getBlockNumber().longValue();
    if (ethLog.getError() != null) {
      if (EthError.MORE_THAN_10000.equals(ethLog.getError().getMessage()) && to > from) {
        // retry by one block
        List<LogObject> logs = Lists.newArrayList();
        for (long i = from; i <= to; i++) {
          DefaultBlockParameterNumber blockNumber = new DefaultBlockParameterNumber(i);
          logs.addAll(
              retrieveEventsInternal(
                  web3jContainer, new EthFilter(blockNumber, blockNumber, ethFilter.getAddress())));
        }

        return logs;
      }
      throw new SyncFailedException(ethLog.getError().getMessage());
    }

    if (ethLog.getLogs() == null) {
      log.warn("retrieveEventsInternal empty logs, from: {}, to: {}", from, to);
      throw new SyncFailedException("empty logs");
    }

    return ethLog.getLogs().stream()
        .map(logResult -> ((EthLog.LogObject) logResult))
        .collect(Collectors.toList());
  }

  @SneakyThrows
  private Map<Long, Block> retrieveBlocksInternal(
      final Web3jContainer web3jContainer,
      final List<DefaultBlockParameterNumber> blockParameterNumbers) {
    int web3jSize = web3jContainer.getWeb3jSize();
    Map<Long, Block> blockMap = new HashMap<>();
    for (DefaultBlockParameterNumber blockParameterNumber : blockParameterNumbers) {
      EthBlock block =
          web3jContainer.getWeb3j().ethGetBlockByNumber(blockParameterNumber, true).send();

      if (block.getError() != null) {
        throw new SyncFailedException(block.getError().getMessage());
      }

      // retry by change another web3j in web3jContainer
      if (web3jSize > 1) {
        int retryTimes = 0;
        while (++retryTimes < web3jSize && block.getBlock() == null) {
          block = web3jContainer.getWeb3j().ethGetBlockByNumber(blockParameterNumber, true).send();
        }
      }
      if (block.getBlock() == null) {
        throw new SyncFailedException(
            "[retrieveBlocksInternal] fail get block: "
                + blockParameterNumber.getBlockNumber().longValue());
      }

      blockMap.put(block.getBlock().getNumber().longValue(), block.getBlock());
    }

    return blockMap;
  }

  @SneakyThrows
  private long getBlockHeightInternal(final Web3jContainer web3jContainer) {

    final EthBlockNumber blockNumber = web3jContainer.getWeb3j().ethBlockNumber().send();

    if (blockNumber.getError() != null) {
      if (blockNumber.getError().getMessage().contains(EthError.REQUEST_LIMIT)
          && web3jContainer.getWeb3jSize() > 1) {
        // retry by change another web3j in web3jContainer
        return retryGetBlockHeight(web3jContainer);
      }
      throw new SyncFailedException(blockNumber.getError().getMessage());
    }

    if (blockNumber.getBlockNumber() == null) {
      throw new SyncFailedException("get block number failed");
    }

    return blockNumber.getBlockNumber().longValue();
  }

  @SneakyThrows
  private long retryGetBlockHeight(final Web3jContainer web3jContainer) {

    EthBlockNumber blockNumber = web3jContainer.getWeb3j().ethBlockNumber().send();

    int retryTimes = 0;
    while (++retryTimes < web3jContainer.getWeb3jSize()
        && blockNumber.getError() != null
        && blockNumber.getError().getMessage().contains(EthError.REQUEST_LIMIT)) {
      blockNumber = web3jContainer.getWeb3j().ethBlockNumber().send();
    }

    if (blockNumber.getError() != null) {
      throw new SyncFailedException(blockNumber.getError().getMessage());
    }

    if (blockNumber.getBlockNumber() == null) {
      throw new SyncFailedException("get block number failed");
    }

    return blockNumber.getBlockNumber().longValue();
  }
}
