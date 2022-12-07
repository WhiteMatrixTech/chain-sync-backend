package com.matrix.blockchain.processor;

import static com.matrix.blockchain.constants.Constants.CONNECTOR;

import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.matrix.blockchain.constants.Constants;
import com.matrix.blockchain.dao.BlockFailedDao;
import com.matrix.blockchain.dao.BlockSuccessDao;
import com.matrix.blockchain.dao.BlockTipDao;
import com.matrix.blockchain.dao.SyncErrorDao;
import com.matrix.blockchain.model.BlockEvent;
import com.matrix.blockchain.model.BlockFailed;
import com.matrix.blockchain.model.BlockList;
import com.matrix.blockchain.model.BlockRange;
import com.matrix.blockchain.model.BlockSuccess;
import com.matrix.blockchain.model.BlockTransaction;
import com.matrix.blockchain.model.BlockchainType;
import com.matrix.blockchain.model.EthereumBlockInfo;
import com.matrix.blockchain.model.FailedBlock;
import com.matrix.blockchain.model.GetTransactionEventsRequest;
import com.matrix.blockchain.model.GetTransactionEventsResponse;
import com.matrix.blockchain.model.SyncError;
import com.matrix.blockchain.model.SyncResponse;
import com.matrix.common.util.ProtobufBeanUtil;
import com.matrix.dynamodb.dao.BaseQueryDao;
import com.matrix.eventhandler.client.BlockchainLogKafkaClient;
import com.matrix.eventhandler.client.BlockchainTransactionHistoryKafkaClient;
import com.matrix.eventhandler.client.BlockchainTransactionKafkaClient;
import com.matrix.eventhandler.model.BlockchainTransactionDTO;
import com.matrix.marketplace.blockchain.model.BlockchainTransaction;
import com.matrix.marketplace.blockchain.util.PaddingUtil;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import schedule.proto.Block;

/**
 * @author luyuanheng
 */
@Log4j2
@Component
public class CommonEventProcessor implements EventProcessor {

  @Resource BlockTipDao blockTipDao;
  @Resource SyncErrorDao syncErrorDao;
  @Resource TransferManager transferManager;
  @Resource Map<String, BlockchainLogKafkaClient> blockchainLogKafkaClientMap;

  @Resource Map<String, BlockchainTransactionKafkaClient> blockchainTransactionKafkaClientMap;

  @Resource
  Map<String, BlockchainTransactionHistoryKafkaClient> blockchainTransactionHistoryKafkaClientMap;

  @Resource BlockSuccessDao blockSuccessDao;

  @Resource BlockFailedDao blockFailedDao;

  @Value("${s3.bucket}")
  private String bucket;

  @Resource AmazonS3 s3Client;

  @Resource Gson gson;

  @Override
  public List<BlockEvent> process(final BlockRange blockRange) {
    return null;
  }

  @Override
  public SyncResponse syncRequest(final BlockList blockList) {
    return null;
  }

  @Override
  public void processBlocks(final SyncResponse syncResponse) {}

  @Override
  public GetTransactionEventsResponse getTransactionEvents(
      final GetTransactionEventsRequest request) {
    return null;
  }

  @Override
  public boolean isApplicable(final String chainType) {
    return false;
  }

  @Override
  public BlockRange resetBlockRange(final BlockRange blockRange) {
    return null;
  }

  public void persistentEvents(
      final BaseQueryDao queryDao, final BlockRange blockRange, final List events) {
    final long start = System.currentTimeMillis();
    if (!CollectionUtils.isEmpty(events)) {
      queryDao.parallelPutItem(events);
    }

    log.info(
        "success persistent events, chainId: {}, range from: {} to: {}, final size: {}, cost: {} mills",
        blockRange.getChainId(),
        blockRange.getFrom(),
        blockRange.getTo(),
        events.size(),
        System.currentTimeMillis() - start);
  }

  public void persistentBlocks(
      final BaseQueryDao queryDao,
      final BlockRange blockRange,
      final Map<Long, EthBlock.Block> blockMap) {
    final long start = System.currentTimeMillis();
    if (!CollectionUtils.isEmpty(blockMap)) {
      blockMap.values().parallelStream()
          .forEach(
              block -> {
                queryDao.putItem(
                    EthereumBlockInfo.builder()
                        .blockNumber(block.getNumber().longValue())
                        .hash(block.getTimestampRaw())
                        .parentHash(block.getParentHash())
                        .timestamp(block.getTimestampRaw())
                        .nonce(block.getNonceRaw())
                        .sha3Uncles(block.getSha3Uncles())
                        .logsBloom(block.getLogsBloom())
                        .transactionsRoot(block.getTransactionsRoot())
                        .stateRoot(block.getStateRoot())
                        .receiptsRoot(block.getReceiptsRoot())
                        .miner(block.getMiner())
                        .difficulty(block.getDifficultyRaw())
                        .totalDifficulty(block.getTotalDifficultyRaw())
                        .size(block.getSizeRaw())
                        .extraData(block.getExtraData())
                        .gasLimit(block.getGasLimitRaw())
                        .gasUsed(block.getGasUsedRaw())
                        .transactionCount(block.getTransactions().size())
                        .baseFeePerGas(block.getBaseFeePerGasRaw())
                        .build());
              });
    }

    log.info(
        "success persistent blocks, chainId: {}, range from: {} to: {}, final size: {}, cost: {} mills",
        blockRange.getChainId(),
        blockRange.getFrom(),
        blockRange.getTo(),
        blockMap.size(),
        System.currentTimeMillis() - start);
  }

  public void persistentTransactions(
      final BaseQueryDao queryDao,
      final BlockRange blockRange,
      final Map<Long, EthBlock.Block> blockMap) {
    final long start = System.currentTimeMillis();

    if (CollectionUtils.isEmpty(blockMap)) {
      return;
    }

    final List<BlockTransaction> list = new ArrayList<>();
    blockMap
        .values()
        .forEach(
            block -> {
              final Map<String, String> blockRaw =
                  Map.of(
                      "number",
                      block.getNumber().toString(),
                      "timestamp",
                      block.getTimestamp().toString(),
                      "transaction_count",
                      String.valueOf(block.getTransactions().size()),
                      "size",
                      block.getSize().toString(),
                      "gas_used",
                      block.getGasUsed().toString());
              final BlockTransaction transaction =
                  BlockTransaction.builder()
                      .blockNumber(block.getNumber().longValue())
                      .transactionHash("0x")
                      .rawData(gson.toJson(blockRaw))
                      .build();
              list.add(transaction);
              block
                  .getTransactions()
                  .forEach(
                      transactionResult -> {
                        if (transactionResult instanceof TransactionObject) {
                          final TransactionObject transactionObject =
                              (TransactionObject) transactionResult;
                          final Map<String, String> transactionRaw =
                              Map.of(
                                  "block_timestamp",
                                  block.getTimestamp().toString(),
                                  "value",
                                  transactionObject.getValue().toString());
                          final BlockTransaction build =
                              BlockTransaction.builder()
                                  .blockNumber(block.getNumber().longValue())
                                  .transactionHash(transactionObject.getHash())
                                  .from(transactionObject.getFrom())
                                  .to(transactionObject.getTo())
                                  .rawData(gson.toJson(transactionRaw))
                                  .build();
                          list.add(build);
                        }
                      });
            });
    queryDao.batchPutItem(list);
    log.info(
        "success persistent transactions, chainId: {}, range from: {} to: {}, final size: {}, cost: {} mills",
        blockRange.getChainId(),
        blockRange.getFrom(),
        blockRange.getTo(),
        blockMap.size(),
        System.currentTimeMillis() - start);
  }

  public void notifyEvents(final BlockRange blockRange, final List<BlockEvent> events) {
    final BlockchainLogKafkaClient blockchainLogKafkaClient =
        blockchainLogKafkaClientMap.get(
            BlockchainType.getBlockchainLogKafkaClient(blockRange.getChainId()));

    if (!CollectionUtils.isEmpty(events)) {
      for (final BlockEvent event : events) {
        blockchainLogKafkaClient.sendDefault(
            blockRange.getChainName() + CONNECTOR + event.getKafkaKey(),
            event.convertToAvro(blockRange));
      }

      log.info(
          "success notify chainId: {}, events, range from: {}, to: {}, events size: {}",
          blockRange.getChainId(),
          blockRange.getFrom(),
          blockRange.getTo(),
          events.size());
    }
  }

  public List<BlockEvent> queryEvents(final BaseQueryDao queryDao, final BlockRange blockRange) {
    final long fromId = blockRange.getFrom() / BlockEvent.BATCH_SIZE;
    final long toId = blockRange.getTo() / BlockEvent.BATCH_SIZE;

    final int fromBlockNumberLen = Long.valueOf(blockRange.getFrom()).toString().length();
    final String fromPadding =
        PaddingUtil.ZERO.repeat(BlockEvent.BLOCK_NUMBER_LEN - fromBlockNumberLen);

    final Long to = blockRange.getTo() + 1;
    final String toPadding =
        PaddingUtil.ZERO.repeat(BlockEvent.BLOCK_NUMBER_LEN - to.toString().length());

    final String logIndexPadding = PaddingUtil.ZERO.repeat(BlockEvent.LOG_INDEX_LEN);

    final String rangeFrom = fromPadding + blockRange.getFrom() + logIndexPadding;
    final String rangeTo = toPadding + to + logIndexPadding;

    if (fromId == toId) {
      // the same partition key
      return queryDao.queryByRange(fromId, rangeFrom, rangeTo);
    } else {
      // the diff partition key
      final Long mid = toId * BlockEvent.BATCH_SIZE;
      final String midPadding =
          PaddingUtil.ZERO.repeat(BlockEvent.BLOCK_NUMBER_LEN - mid.toString().length());
      final String rangeMid = midPadding + mid + logIndexPadding;

      final List<BlockEvent> events = new ArrayList<>();
      events.addAll(queryDao.queryByRange(fromId, rangeFrom, rangeMid));
      events.addAll(queryDao.queryByRange(toId, rangeMid, rangeTo));
      return events;
    }
  }

  public Map<String, BlockEvent> getEventMap(
      final BaseQueryDao baseQueryDao, final BlockRange blockRange) {
    final Map<String, BlockEvent> eventMap = new HashMap<>();
    final List<BlockEvent> items = this.queryEvents(baseQueryDao, blockRange);
    if (items != null) {
      eventMap.putAll(
          items.stream().collect(Collectors.toMap(BlockEvent::getKey, Function.identity())));
    }

    return eventMap;
  }

  public void persistentError(final BlockRange blockRange, final Exception error) {
    final SyncError syncError =
        SyncError.builder()
            .blockNumber(blockRange.getFrom())
            .errorDetail(
                blockRange.getTo()
                    + CONNECTOR
                    + StringUtils.abbreviate(
                        Throwables.getStackTraceAsString(error), Constants.ERROR_STRING_MAX_WIDTH))
            .errorType(error.getClass().getCanonicalName())
            .chainType(blockRange.getChainId())
            .updatedAt(Instant.now())
            .build();
    syncErrorDao.putItem(syncError);
  }

  protected boolean isExceeded(final String data) {
    return StringUtils.isNotBlank(data) && data.length() > Constants.DATA_MAX_SIZE;
  }

  @SneakyThrows
  protected String uploadS3(final String data, final String folder, final String key) {
    final Upload upload =
        transferManager.upload(
            bucket, key, new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)), null);
    upload.waitForCompletion();

    return folder + Constants.FILE_PATH_SEPARATOR + key;
  }

  protected String getEventDataS3Key(
      final String chainType, final String txHash, final Long logIndex) {
    return chainType + CONNECTOR + txHash + CONNECTOR + logIndex;
  }

  protected String getBlockDataS3Key(final String chainType, final String blockNumber) {
    return chainType + CONNECTOR + blockNumber;
  }

  protected void notifyTransactions(
      final BlockList blockList, final List<BlockchainTransaction> transactions) {
    final BlockchainTransactionKafkaClient blockchainTransactionKafkaClient =
        blockchainTransactionKafkaClientMap.get(
            BlockchainType.getBlockchainTransactionKafkaClient(blockList.getChainId()));

    if (!CollectionUtils.isEmpty(transactions)) {
      for (final BlockchainTransaction transaction : transactions) {
        blockchainTransactionKafkaClient.sendDefault(
            transaction.getChainType() + CONNECTOR + transaction.getBlockNumber(),
            BlockchainTransactionDTO.newBuilder()
                .setChainType(blockList.getChainType())
                .setChainName(blockList.getChainName())
                .setBlockNumber(transaction.getBlockNumber())
                .setTransactionHash(transaction.getTransactionHash())
                .setEvents(transaction.getEvents())
                .build());
      }

      log.info(
          "success notify {} transactions, blockNumbers: {}, transactions size: {}",
          blockList.getChainId(),
          blockList.getBlockNumbersList(),
          transactions.size());
    }
  }

  protected void notifyTransactionsHistory(
      final BlockList blockList, final List<BlockchainTransaction> transactions) {
    final BlockchainTransactionHistoryKafkaClient blockchainTransactionKafkaClient =
        blockchainTransactionHistoryKafkaClientMap.get(
            BlockchainType.getBlockchainTransactionHistoryKafkaClient(blockList.getChainId()));

    if (!CollectionUtils.isEmpty(transactions)) {
      for (final BlockchainTransaction transaction : transactions) {
        blockchainTransactionKafkaClient.sendDefault(
            transaction.getChainType() + CONNECTOR + transaction.getBlockNumber(),
            BlockchainTransactionDTO.newBuilder()
                .setChainType(blockList.getChainType())
                .setChainName(blockList.getChainName())
                .setBlockNumber(transaction.getBlockNumber())
                .setTransactionHash(transaction.getTransactionHash())
                .setEvents(transaction.getEvents())
                .build());
      }

      log.info(
          "success notify {} transactions, blockNumbers: {}, transactions size: {}",
          blockList.getChainId(),
          blockList.getBlockNumbersList(),
          transactions.size());
    }
  }

  @SneakyThrows
  public List<Block> getBlocksFromS3(
      final String chainType,
      final String chainName,
      final String bucket,
      final List<Long> blockNumbers) {
    final List<Block> blocks = Lists.newArrayList();

    blockNumbers.parallelStream()
        .forEach(
            blockNumber -> {
              final String key = getBlockStorePath(chainType, chainName, blockNumber);
              final S3Object object = s3Client.getObject(bucket, key);
              try {
                blocks.add(parseBlock(object.getObjectContent()));
              } catch (final IOException e) {
                log.error("get block info from s3 failed, key: {}, error: {}", key, e);
                throw new RuntimeException(e);
              }
            });

    Collections.sort(blocks, (b1, b2) -> (int) (b1.getHeight() - b2.getHeight()));
    return blocks;
  }

  private Block parseBlock(final InputStream input) throws IOException {
    try (input) {
      final StringBuilder content = new StringBuilder();
      final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
      String line = null;
      while ((line = reader.readLine()) != null) {
        content.append(line);
      }

      final Block.Builder builder = Block.newBuilder();
      ProtobufBeanUtil.toProtoBean(builder, getBlockJson(content.toString()));

      return builder.build();
    } catch (final IOException e) {
      throw new IOException(e);
    }
  }

  private JsonObject getBlockJson(final String content) {
    final JsonObject blockJson = gson.fromJson(content.toString(), JsonObject.class);
    if (blockJson.has("collections")) {
      blockJson.remove("collections");
    }
    if (blockJson.has("blockMetadata")) {
      blockJson.remove("blockMetadata");
    }
    return blockJson;
  }

  private String getBlockStorePath(
      final String chainType, final String chainName, final Long blockNumber) {
    return chainType + "/" + chainName + "/" + blockNumber;
  }

  protected void saveSuccessBlocks(final BlockList blockList, final List<Long> blockNumbers) {
    if (!CollectionUtils.isEmpty(blockNumbers)) {
      final List<BlockSuccess> blocks = Lists.newArrayList();
      for (final Long blockNumber : blockNumbers) {
        blocks.add(
            BlockSuccess.builder()
                .blockNumber(blockNumber)
                .chainId(blockList.getChainId())
                .build());
      }
      blockSuccessDao.batchPutItem(blocks);
    }
  }

  protected List<Long> querySuccessBlocks(final BlockList blockList) {
    final List<Long> successList = Lists.newArrayList();
    if (blockList.getBlockNumbersCount() > 0) {
      final List<PrimaryKey> keys = Lists.newArrayList();
      for (final Long blockNumber : blockList.getBlockNumbersList()) {
        keys.add(
            new PrimaryKey(
                BlockSuccess.ATTR_CHAIN_ID,
                blockList.getChainId(),
                BlockSuccess.ATTR_BLOCK_NUMBER,
                blockNumber));
      }

      final List<BlockSuccess> blockSuccesses = blockSuccessDao.batchGetItems(keys);
      if (!CollectionUtils.isEmpty(blockSuccesses)) {
        blockSuccesses.stream()
            .forEach(
                block -> {
                  successList.add(block.getBlockNumber());
                });
      }
    }

    return successList;
  }

  protected void saveFailedBlocks(final BlockList blockList, final List<FailedBlock> blocks) {
    if (!CollectionUtils.isEmpty(blocks)) {
      final List<BlockFailed> failedList = Lists.newArrayList();
      for (final FailedBlock block : blocks) {
        failedList.add(
            BlockFailed.builder()
                .chainId(blockList.getChainId())
                .blockNumber(block.getHeight())
                .errorMessage(block.getErrorMessage())
                .build());
      }
      blockFailedDao.batchPutItem(failedList);
    }
  }
}
