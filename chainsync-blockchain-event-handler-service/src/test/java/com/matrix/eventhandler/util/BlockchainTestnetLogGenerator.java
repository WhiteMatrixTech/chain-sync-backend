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
public class BlockchainTestnetLogGenerator {

  private static final Web3j web3j =
      Web3j.build(
          new HttpService("https://eth-rinkeby.alchemyapi.io/v2/XAPTAFurz8yTmLyCjWVASBf8MLLwblby"));

  public static BlockchainEventLogDTO getPhantaBearLog() {
    // transaction
    // https://rinkeby.etherscan.io/tx/0xcc3529043439346b580a86684068f35b638ed2921e018560d77a1c65dfd15030
    return BlockchainEventLogDTO.newBuilder()
        .setRemoved(false)
        .setAddress("0xA9D29DB8B0A2e87482B7D2EaD732aaf161dAC3D9")
        .setBlockNumber(10627672L)
        .setChainType("ethereum")
        .setChainName("rinkeby")
        .setData("0x")
        .setLogIndex(54)
        .setType("null")
        .setTopics(
            List.of(
                "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                "0x0000000000000000000000007e3b889e45fda81221b58c64d48f1858b81fac41",
                "0x000000000000000000000000a4d959c98e2fbb6f73f07ed91913c497cea713b6",
                "0x0000000000000000000000000000000000000000000000000000000000002612"))
        .setBlockTimestamp(1)
        .setTransactionHash("0x31de87ca938e37b670145fa9a5a797e76141b9c30f6df1586c126646f0aef965")
        .setTransactionIndex(1)
        .setBlockHash("")
        .build();
  }

  public static BlockchainEventLogDTO getPhancyPetLog() {
    // transaction
    // https://rinkeby.etherscan.io/tx/0xcc3529043439346b580a86684068f35b638ed2921e018560d77a1c65dfd15030
    return BlockchainEventLogDTO.newBuilder()
        .setRemoved(false)
        .setAddress("0x554478E4c47EF61806fab268d1B74543d4D01f91")
        .setBlockNumber(10627515L)
        .setChainType("ethereum")
        .setChainName("rinkeby")
        .setData("0x")
        .setLogIndex(115)
        .setType("null")
        .setTopics(
            List.of(
                "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                "0x00000000000000000000000067f00fc4ab4bcf27f1b3a50ff1e86a4a2046c26b",
                "0x000000000000000000000000d001634059c78d1cb7a3a0518e1881e954194127",
                "0x000000000000000000000000000000000000000000000000000000000000012e"))
        .setBlockTimestamp(1)
        .setTransactionHash("0xcc3529043439346b580a86684068f35b638ed2921e018560d77a1c65dfd15030")
        .setTransactionIndex(1)
        .setBlockHash("")
        .build();
  }

  public static List<BlockchainEventLogDTO> ThiersverseBlockLogs() {
    return obtainBlockLogs(11009046L);
  }

  public static List<BlockchainEventLogDTO> phantaPetsBlockLogs() {
    return obtainBlockLogs(10627515L);
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
                  .setChainName("rinkeby")
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
