package com.matrix.etl.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.protobuf.Empty;
import com.matrix.etl.dao.BlockTipDao;
import com.matrix.etl.dao.ETHTransactionDao;
import com.matrix.etl.dao.EthereumBlockEventDao;
import com.matrix.etl.dao.TaskDao;
import com.matrix.etl.dao.TaskDefDao;
import com.matrix.etl.model.BlockTip;
import com.matrix.etl.model.BlockTransaction;
import com.matrix.etl.model.ChainType;
import com.matrix.etl.model.EthereumBlockEvent;
import com.matrix.etl.model.HandlerType;
import com.matrix.etl.model.SimpleBlock;
import com.matrix.etl.model.SimpleHandler;
import com.matrix.etl.model.SimpleTask;
import com.matrix.etl.model.SimpleTransaction;
import com.matrix.etl.model.Task;
import com.matrix.etl.model.TaskDef;
import com.matrix.etl.model.response.QueryBlockResponse;
import com.matrix.etl.model.response.QueryEventsResponse;
import com.matrix.etl.model.response.QueryHandlerResponse;
import com.matrix.etl.model.response.QueryTaskLogResponse;
import com.matrix.etl.model.response.QueryTaskResponse;
import com.matrix.etl.model.response.QueryTransactionResponse;
import com.matrix.etl.service.BlockchainService;
import com.matrix.eventhandler.model.BlockchainEventHandlerServiceGrpc.BlockchainEventHandlerServiceBlockingStub;
import com.matrix.eventhandler.model.BlockchainEventHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.client.inject.GrpcClient;
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

  @Resource BlockTipDao blockTipDao;

  @GrpcClient("matrix-cloud-blockchain-event-handler-service")
  BlockchainEventHandlerServiceBlockingStub blockchainEventHandlerServiceBlockingStub;

  static Gson GSON = new Gson();


  @SneakyThrows
  @Override
  public QueryTaskResponse queryTask() {
    final List<SimpleTask> simpleTasks = new ArrayList<>();
    final List<TaskDef> taskDefs = taskDefDao.scan();
    // format create time
    taskDefs.forEach(
        taskDef -> {
          final String status;
          final String taskType;
          Long blockNumber = null;
          if (taskDef.getCreateTime().toString().length() == 10) {
            taskDef.setCreateTime(taskDef.getCreateTime() * 1000);
          }
          if (Boolean.TRUE.equals(taskDef.getDelete())) {
            status = "paused";
          } else {
            status = "active";
          }
          final Map map = GSON.fromJson(taskDef.getParams(), Map.class);
          if (map.get("end") == null && !"sync_retry_request".equals(taskDef.getTaskType())) {
            taskType = "SYNC_LATEST";
          } else {
            taskType = "BACK_FILL";
          }
          if ("SYNC_LATEST".equals(taskType)) {
            final BlockTip item =
                blockTipDao.getItem(map.get("chainName") + "_" + map.get("chainType"));
            blockNumber = item.getBlockNumber();
          }
          final SimpleTask simpleTask =
              SimpleTask.builder()
                  .taskType(taskType)
                  .taskName(taskDef.getTaskName())
                  .status(status)
                  .blockchain((String) map.get("chainType"))
                  .createTime(taskDef.getCreateTime())
                  .params(taskDef.getParams())
                  .blockNumber(blockNumber)
                  .build();
          simpleTasks.add(simpleTask);
        });
    return QueryTaskResponse.builder().tasks(simpleTasks).build();
  }

  @Override
  public QueryTaskLogResponse queryTaskName(final String taskName) {
    final List<Task> tasks = taskDao.queryWithLimit(taskName, 50);
    return QueryTaskLogResponse.builder().tasks(tasks).build();
  }

  @Override
  public QueryHandlerResponse queryHandler() {
    final BlockchainEventHandlers eventHandlers =
        blockchainEventHandlerServiceBlockingStub.getHandlers(Empty.getDefaultInstance());
    return QueryHandlerResponse.builder()
        .handlers(
            eventHandlers.getHandlersList().stream()
                .map(
                    handler ->
                        SimpleHandler.builder()
                            .blockchain(ChainType.ethereum)
                            .handlerName(handler.getName())
                            .type(
                                "Default".equals(handler.getGroup())
                                    ? HandlerType.DEFAULT
                                    : HandlerType.CUSTOM)
                            .appName(handler.getGroup())
                            .build())
                .collect(Collectors.toList()))
        .build();
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
  public QueryTransactionResponse queryTransaction(final String chainType, final Long blockNumber) {
    final List<BlockTransaction> blockTransactions;
    if (blockNumber != null) {
      blockTransactions =
          ethTransactionDao.queryByPartitionKeyWithLimit(blockNumber, 50).stream()
              .filter(
                  blockTransaction ->
                      !BlockTransaction.ROOT_TRANSACTION_HASH.equals(
                          blockTransaction.getTransactionHash()))
              .collect(Collectors.toList());
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
                          .from(blockTransaction.getFrom())
                          .to(blockTransaction.getTo())
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
