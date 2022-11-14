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
import com.matrix.blockchain.model.BlockchainType;
import com.matrix.blockchain.model.FailedBlock;
import com.matrix.blockchain.model.GetTransactionEventsRequest;
import com.matrix.blockchain.model.GetTransactionEventsResponse;
import com.matrix.blockchain.model.NotifyStatus;
import com.matrix.blockchain.model.SyncError;
import com.matrix.blockchain.model.SyncResponse;
import com.matrix.common.util.ProtobufBeanUtil;
import com.matrix.dynamodb.dao.BaseQueryDao;
import com.matrix.eventhandler.client.BlockchainLogKafkaClient;
import com.matrix.eventhandler.client.BlockchainTransactionHistoryKafkaClient;
import com.matrix.eventhandler.client.BlockchainTransactionKafkaClient;
import com.matrix.eventhandler.model.BlockchainTransactionDTO;
import com.matrix.marketplace.blockchain.dao.BlockchainTransactionDao;
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

  @Resource Map<String, BlockchainTransactionDao> blockchainTransactionDaoMap;

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
  public SyncResponse syncRequest(BlockList blockList) {
    return null;
  }

  @Override
  public void processBlocks(SyncResponse syncResponse) {}

  @Override
  public GetTransactionEventsResponse getTransactionEvents(GetTransactionEventsRequest request) {
    return null;
  }

  @Override
  public boolean isApplicable(final String chainType) {
    return false;
  }

  @Override
  public BlockRange resetBlockRange(BlockRange blockRange) {
    return null;
  }

  public void persistentEvents(
      final BaseQueryDao queryDao, final BlockRange blockRange, final List events) {
    long start = System.currentTimeMillis();
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

  public void notifyEvents(final BlockRange blockRange, final List<BlockEvent> events) {
    BlockchainLogKafkaClient blockchainLogKafkaClient =
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

  public void processTransactions(
      final BlockRange blockRange, final List<BlockchainTransaction> transactions) {
    // persistent
    persistentTransactions(blockRange, transactions);
    // notify
    notifyTransactions(blockRange, transactions);
    // update status
    updateTransactionStatus(blockRange, transactions);
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

  public Map<String, BlockchainTransaction> getTransactionMap(final BlockRange blockRange) {
    BlockchainTransactionDao blockchainTransactionDao =
        this.getBlockchainTransactionDao(blockRange);

    final Map<String, BlockchainTransaction> transactionHashMap = new HashMap<>();
    final List<BlockchainTransaction> items =
        blockchainTransactionDao.queryTransactions(
            com.matrix.marketplace.blockchain.model.BlockRange.builder()
                .chainType(blockRange.getChainType())
                .from(blockRange.getFrom())
                .to(blockRange.getTo())
                .build());
    if (items != null) {
      transactionHashMap.putAll(
          items.stream()
              .collect(
                  Collectors.toMap(
                      BlockchainTransaction::getTransactionHash, Function.identity())));
    }

    return transactionHashMap;
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

  private void persistentTransactions(final BlockRange blockRange, final List transactions) {

    BlockchainTransactionDao blockchainTransactionDao =
        this.getBlockchainTransactionDao(blockRange);

    if (!CollectionUtils.isEmpty(transactions)) {
      blockchainTransactionDao.parallelPutItem(transactions);
    }

    log.info(
        "success persistent transactions, chainId: {}, range from: {} to: {}, final size: {}",
        blockRange.getChainId(),
        blockRange.getFrom(),
        blockRange.getTo(),
        transactions.size());
  }

  protected void notifyTransactions(
      final BlockRange blockRange, final List<BlockchainTransaction> transactions) {
    BlockchainTransactionKafkaClient blockchainTransactionKafkaClient =
        blockchainTransactionKafkaClientMap.get(
            BlockchainType.getBlockchainTransactionKafkaClient(blockRange.getChainId()));

    if (!CollectionUtils.isEmpty(transactions)) {
      for (final BlockchainTransaction transaction : transactions) {
        blockchainTransactionKafkaClient.sendDefault(
            transaction.getChainType() + CONNECTOR + transaction.getBlockNumber(),
            BlockchainTransactionDTO.newBuilder()
                .setChainType(blockRange.getChainType())
                .setChainName(blockRange.getChainName())
                .setBlockNumber(transaction.getBlockNumber())
                .setTransactionHash(transaction.getTransactionHash())
                .setEvents(transaction.getEvents())
                .build());
      }

      log.info(
          "success notify {} transactions, range from: {}, to: {}, transactions size: {}",
          blockRange.getChainId(),
          blockRange.getFrom(),
          blockRange.getTo(),
          transactions.size());
    }
  }

  protected void notifyTransactions(
      final BlockList blockList, final List<BlockchainTransaction> transactions) {
    BlockchainTransactionKafkaClient blockchainTransactionKafkaClient =
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
    BlockchainTransactionHistoryKafkaClient blockchainTransactionKafkaClient =
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

  private void updateTransactionStatus(
      final BlockRange blockRange, final List<BlockchainTransaction> transactions) {
    BlockchainTransactionDao blockchainTransactionDao =
        this.getBlockchainTransactionDao(blockRange);
    final Instant now = Instant.now();
    for (final BlockchainTransaction transaction : transactions) {
      transaction.setStatus(NotifyStatus.SENT.name());
      transaction.setUpdatedAt(now);
    }
    blockchainTransactionDao.parallelPutItem(transactions);
  }

  private BlockchainTransactionDao getBlockchainTransactionDao(BlockRange blockRange) {
    BlockchainTransactionDao blockchainTransactionDao =
        blockchainTransactionDaoMap.get(
            BlockchainType.getBlockchainTransactionDao(blockRange.getChainId()));

    return blockchainTransactionDao;
  }

  @SneakyThrows
  public List<Block> getBlocksFromS3(
      String chainType, String chainName, String bucket, List<Long> blockNumbers) {
    List<Block> blocks = Lists.newArrayList();

    blockNumbers.parallelStream()
        .forEach(
            blockNumber -> {
              String key = getBlockStorePath(chainType, chainName, blockNumber);
              S3Object object = s3Client.getObject(bucket, key);
              try {
                blocks.add(parseBlock(object.getObjectContent()));
              } catch (IOException e) {
                log.error("get block info from s3 failed, key: {}, error: {}", key, e);
                throw new RuntimeException(e);
              }
            });

    Collections.sort(blocks, (b1, b2) -> (int) (b1.getHeight() - b2.getHeight()));
    return blocks;
  }

  private Block parseBlock(InputStream input) throws IOException {
    try (input) {
      StringBuilder content = new StringBuilder();
      BufferedReader reader = new BufferedReader(new InputStreamReader(input));
      String line = null;
      while ((line = reader.readLine()) != null) {
        content.append(line);
      }

      Block.Builder builder = Block.newBuilder();
      ProtobufBeanUtil.toProtoBean(builder, getBlockJson(content.toString()));

      return builder.build();
    } catch (IOException e) {
      throw new IOException(e);
    }
  }

  private JsonObject getBlockJson(String content) {
    JsonObject blockJson = gson.fromJson(content.toString(), JsonObject.class);
    if (blockJson.has("collections")) {
      blockJson.remove("collections");
    }
    if (blockJson.has("blockMetadata")) {
      blockJson.remove("blockMetadata");
    }
    return blockJson;
  }

  private String getBlockStorePath(String chainType, String chainName, Long blockNumber) {
    return chainType + "/" + chainName + "/" + blockNumber;
  }

  protected void saveSuccessBlocks(final BlockList blockList, final List<Long> blockNumbers) {
    if (!CollectionUtils.isEmpty(blockNumbers)) {
      List<BlockSuccess> blocks = Lists.newArrayList();
      for (Long blockNumber : blockNumbers) {
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
    List<Long> successList = Lists.newArrayList();
    if (blockList.getBlockNumbersCount() > 0) {
      List<PrimaryKey> keys = Lists.newArrayList();
      for (Long blockNumber : blockList.getBlockNumbersList()) {
        keys.add(
            new PrimaryKey(
                BlockSuccess.ATTR_CHAIN_ID,
                blockList.getChainId(),
                BlockSuccess.ATTR_BLOCK_NUMBER,
                blockNumber));
      }

      List<BlockSuccess> blockSuccesses = blockSuccessDao.batchGetItems(keys);
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
      List<BlockFailed> failedList = Lists.newArrayList();
      for (FailedBlock block : blocks) {
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
