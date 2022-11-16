package com.matrix.eventhandler.util;

import com.matrix.eventhandler.model.BlockchainEventLogDTO;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthLog.LogObject;
import org.web3j.protocol.http.HttpService;

/**
 * @author reimia
 */
public class BlockchainMainnetLogGenerator {

  private static final Web3j web3j =
      Web3j.build(
          new HttpService("https://eth-mainnet.alchemyapi.io/v2/XAPTAFurz8yTmLyCjWVASBf8MLLwblby"));

  public static List<BlockchainEventLogDTO> theirsverseMultiLogs() {
    return obtainBlockLogs(15578953L);
  }

  @SneakyThrows
  private static List<BlockchainEventLogDTO> obtainBlockLogs(final long blockNum) {
    final EthLog ethLog =
        web3j
            .ethGetLogs(
                new EthFilter(
                    DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNum)),
                    DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNum)),
                    Collections.emptyList()))
            .send();

    return ethLog.getLogs().stream()
        .map(
            logResult -> {
              final LogObject log = (LogObject) logResult;
              return BlockchainEventLogDTO.newBuilder()
                  .setRemoved(log.isRemoved())
                  .setAddress(log.getAddress())
                  .setBlockNumber(log.getBlockNumber().longValue())
                  .setChainType("ethereum")
                  .setChainName("mainnet")
                  .setData(log.getData())
                  .setLogIndex(log.getLogIndex().longValue())
                  .setType(log.getType())
                  .setTopics(log.getTopics())
                  .setBlockTimestamp(1)
                  .setTransactionHash(log.getTransactionHash())
                  .setTransactionIndex(log.getTransactionIndex().longValue())
                  .setBlockHash(log.getBlockHash())
                  .build();
            })
        .collect(Collectors.toList());
  }
}
