package com.matrix.etl.service.impl;

import com.amazonaws.services.dynamodbv2.document.QueryFilter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.matrix.blockchain.dao.ETHTransactionDao;
import com.matrix.blockchain.model.BlockTransaction;
import com.matrix.dynamodb.model.CursorQuerySpec;
import com.matrix.etl.dao.EthereumBlockEventDao;
import com.matrix.etl.dao.TaskDao;
import com.matrix.etl.dao.TaskDefDao;
import com.matrix.etl.model.ChainType;
import com.matrix.etl.model.EthereumBlockEvent;
import com.matrix.etl.model.HandlerType;
import com.matrix.etl.model.SimpleApp;
import com.matrix.etl.model.SimpleBlock;
import com.matrix.etl.model.SimpleHandler;
import com.matrix.etl.model.SimpleTask;
import com.matrix.etl.model.SimpleTransaction;
import com.matrix.etl.model.Task;
import com.matrix.etl.model.TaskDef;
import com.matrix.etl.model.response.QueryAppResponse;
import com.matrix.etl.model.response.QueryBlockResponse;
import com.matrix.etl.model.response.QueryEventsResponse;
import com.matrix.etl.model.response.QueryHandlerResponse;
import com.matrix.etl.model.response.QueryTaskResponse;
import com.matrix.etl.model.response.QueryTransactionResponse;
import com.matrix.etl.service.BlockchainService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Resource;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * @author richard
 */
@Log4j2
@Service
public class BlockchainServiceImpl implements BlockchainService {

  @Resource TaskDefDao taskDefDao;

  @Resource TaskDao taskDao;

  @Resource ETHTransactionDao ethTransactionDao;

  @Resource EthereumBlockEventDao ethereumBlockEventDao;

  static Gson GSON = new Gson();

  LoadingCache<String, Map<String, String>> taskDefCache =
      CacheBuilder.newBuilder()
          .expireAfterAccess(10, TimeUnit.MINUTES)
          .build(
              new CacheLoader<>() {
                @Override
                public Map<String, String> load(@NonNull final String key) {
                  return taskDefDao.scan().stream()
                      .collect(
                          Collectors.toMap(
                              TaskDef::getTaskName,
                              taskDef -> {
                                final Map<String, Object> map =
                                    GSON.fromJson(taskDef.getParams(), HashMap.class);
                                return map.get("chainType") == null
                                    ? ""
                                    : (String) map.get("chainType");
                              }));
                }
              });

  @SneakyThrows
  @Override
  public QueryTaskResponse queryTask() {
    final Map<String, String> taskNameToChainTypeMap = taskDefCache.get("taskDef");
    return QueryTaskResponse.builder()
        .tasks(
            Stream.of(
                    taskDao
                        .queryByCursor(
                            "sync_request_flow_mainnet_spork13_reverse",
                            CursorQuerySpec.builder().limit(50).build(),
                            new QueryFilter(Task.ATTR_STATUS).in("SUCCESS", "PROCESSING"))
                        .getItems(),
                    taskDao
                        .queryByCursor(
                            "sync_retry_mainnet_ethereum",
                            CursorQuerySpec.builder().limit(50).build(),
                            new QueryFilter(Task.ATTR_STATUS).in("SUCCESS", "PROCESSING"))
                        .getItems())
                .flatMap(List::stream)
                .map(
                    task ->
                        SimpleTask.builder()
                            .blockchain(taskNameToChainTypeMap.get(task.getTaskName()))
                            .taskName(task.getTaskName())
                            .taskId(task.getTaskId())
                            .createTime(task.getCreateTime())
                            .status(task.getStatus())
                            .build())
                .collect(Collectors.toList()))
        .build();
  }

  @Override
  public QueryHandlerResponse queryHandler() {
    final List<SimpleHandler> handlers = new ArrayList<>();
    handlers.add(
        SimpleHandler.builder()
            .blockchain(ChainType.ethereum)
            .handlerName("OneSyncEnrollmentEventHandler")
            .type(HandlerType.CUSTOM)
            .build());
    handlers.add(
        SimpleHandler.builder()
            .blockchain(ChainType.ethereum)
            .handlerName("OneSyncNotifierEventHandler")
            .type(HandlerType.CUSTOM)
            .build());
    handlers.add(
        SimpleHandler.builder()
            .blockchain(ChainType.ethereum)
            .handlerName("OneSyncTokenOwnershipUpdateEventHandler")
            .type(HandlerType.CUSTOM)
            .build());
    handlers.add(
        SimpleHandler.builder()
            .blockchain(ChainType.ethereum)
            .handlerName("BlockchainEventHandler")
            .type(HandlerType.DEFAULT)
            .build());
    handlers.add(
        SimpleHandler.builder()
            .blockchain(ChainType.ethereum)
            .handlerName("PhantaBearEventHandler")
            .type(HandlerType.CUSTOM)
            .build());
    handlers.add(
        SimpleHandler.builder()
            .blockchain(ChainType.ethereum)
            .handlerName("PhantaDogEventHandler")
            .type(HandlerType.CUSTOM)
            .build());
    handlers.add(
        SimpleHandler.builder()
            .blockchain(ChainType.ethereum)
            .handlerName("TheirsverseTransferEventHandler")
            .type(HandlerType.CUSTOM)
            .build());
    return QueryHandlerResponse.builder().handlers(handlers).build();
  }

  @Override
  public QueryAppResponse queryApp() {
    final List<SimpleApp> apps = new ArrayList<>();
    apps.add(
        SimpleApp.builder()
            .blockchain(ChainType.ethereum)
            .appName("OneSync")
            .handlers(
                List.of(
                    "OneSyncEnrollmentEventHandler",
                    "OneSyncNotifierEventHandler",
                    "OneSyncTokenOwnershipUpdateEventHandler"))
            .build());
    apps.add(
        SimpleApp.builder()
            .blockchain(ChainType.ethereum)
            .appName("Phantaci")
            .handlers(List.of("PhantaBearEventHandler", "PhantaDogEventHandler"))
            .build());
    apps.add(
        SimpleApp.builder()
            .blockchain(ChainType.ethereum)
            .appName("Theirsverse")
            .handlers(List.of("TheirsverseTransferEventHandler"))
            .build());
    return QueryAppResponse.builder().apps(apps).build();
  }

  @Override
  public QueryBlockResponse queryBlock(final String chainType, final Long blockNumber) {
    final List<BlockTransaction> blockTransactions;
    if (blockNumber != null) {
      blockTransactions =
          List.of(ethTransactionDao.getItem(blockNumber, BlockTransaction.ROOT_TRANSACTION_HASH));
    } else {
      blockTransactions =
          ethTransactionDao.queryIndexWithLimit(
              BlockTransaction.INDEX_TRANSACTION_HASH, BlockTransaction.ROOT_TRANSACTION_HASH, 50);
    }
    return QueryBlockResponse.builder()
        .blocks(
            blockTransactions.stream()
                .map(
                    block -> {
                      final JsonObject blockJson =
                          GSON.fromJson(block.getRawData(), JsonObject.class);
                      return SimpleBlock.builder()
                          .blockNumber(blockJson.get("number").getAsLong())
                          .timestamp(blockJson.get("timestamp").getAsString())
                          .transactionCount(blockJson.get("transaction_count").getAsInt())
                          .size(blockJson.get("size").getAsInt())
                          .gasUsed(blockJson.get("gas_used").getAsLong())
                          .build();
                    })
                .collect(Collectors.toList()))
        .build();
  }

  @Override
  public QueryTransactionResponse queryTransaction(
      final String chainType, final String transactionHash) {
    final List<BlockTransaction> blockTransactions;
    if (transactionHash != null) {
      blockTransactions =
          ethTransactionDao.queryByPartitionKeyOnGsi(
              BlockTransaction.INDEX_TRANSACTION_HASH, transactionHash);
    } else {
      blockTransactions = ethTransactionDao.scanWithLimit(50);
    }
    return QueryTransactionResponse.builder()
        .transactions(
            blockTransactions.stream()
                .map(
                    blockTransaction -> {
                      final JsonObject blockJson =
                          GSON.fromJson(blockTransaction.getRawData(), JsonObject.class);
                      return SimpleTransaction.builder()
                          .blockNumber(blockTransaction.getBlockNumber())
                          .transactionHash(blockTransaction.getTransactionHash())
                          .timestamp(blockJson.get("block_timestamp").getAsString())
                          .from(blockJson.get("from_address").getAsString())
                          .to(blockJson.get("to_address").getAsString())
                          .value(blockJson.get("value").getAsString())
                          .build();
                    })
                .collect(Collectors.toList()))
        .build();
  }

  @Override
  public QueryEventsResponse queryEvents(final String chainType) {
    final ChainType type = ChainType.valueOf(chainType);
    if (type.equals(ChainType.ethereum)) {
      final List<EthereumBlockEvent> ethereumBlockEvents = ethereumBlockEventDao.scanWithLimit(50);
      return QueryEventsResponse.builder().events(ethereumBlockEvents).build();
    }
    return null;
  }
}