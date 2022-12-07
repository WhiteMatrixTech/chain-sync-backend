package com.chainsync.dynamodb.dao;

import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.BatchGetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.Page;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.QueryFilter;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition;
import com.amazonaws.services.dynamodbv2.document.ScanFilter;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.api.QueryApi;
import com.amazonaws.services.dynamodbv2.document.api.ScanApi;
import com.amazonaws.services.dynamodbv2.document.spec.BatchGetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateGlobalSecondaryIndexAction;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndexDescription;
import com.amazonaws.services.dynamodbv2.model.GlobalTableAlreadyExistsException;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.amazonaws.services.dynamodbv2.model.Select;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.chainsync.dynamodb.model.PaginationDTO;
import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
import com.chainsync.dynamodb.orm.dynamo.IndexDefinition;
import com.chainsync.dynamodb.orm.dynamo.TableDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;

/**
 * This is the Dao class built on top of AWS DynamoDB V2 APIs s
 *
 * @param <OrmType>
 */
@Log4j2
@Getter
public abstract class BaseQueryDao<OrmType> {

  private static final int BATCH_WRITE_SIZE = 25;
  private static final int LIMIT = 10000;
  @NonNull private final DynamoDBTableOrmManager<OrmType> ormManager;
  @NonNull private final DynamoDB dynamoDB;
  @NonNull TableDefinition tableDefinition;

  public BaseQueryDao(final DynamoDBTableOrmManager<OrmType> ormManager, final DynamoDB dynamoDB) {
    this.ormManager = ormManager;
    this.tableDefinition = ormManager.getTableDefinition();
    this.dynamoDB = dynamoDB;
    createTable();
    checkGSI();
  }

  @SneakyThrows
  private void createTable() {
    try {
      log.info("Creating table named {}", tableDefinition.getTableName());
      dynamoDB.createTable(tableDefinition.toCreateTableRequest()).waitForActive();
    } catch (final GlobalTableAlreadyExistsException | ResourceInUseException e) {
      log.info("Table already exits, skip creation");
    }
  }

  private void checkGSI() {
    log.info("Checking GSI on {}", tableDefinition.getTableName());
    final Table table = dynamoDB.getTable(tableDefinition.getTableName());
    final List<GlobalSecondaryIndexDescription> remoteIndexes =
        Optional.ofNullable(table.describe().getGlobalSecondaryIndexes()).orElse(new ArrayList<>());
    final List<String> remoteIndexNames =
        remoteIndexes.stream()
            .map(GlobalSecondaryIndexDescription::getIndexName)
            .collect(Collectors.toList());
    if (remoteIndexNames.containsAll(tableDefinition.getGlobalSecondaryIndices().keySet())) {
      log.info("GSI checked finished, no need add");
    } else {
      tableDefinition
          .getGlobalSecondaryIndices()
          .forEach(
              (indexName, indexDefinition) -> {
                if (!remoteIndexNames.contains(indexName)) {
                  final List<AttributeDefinition> attributeDefinitions =
                      indexDefinition.toAttributeDefinition();
                  if (attributeDefinitions.size() == 1) {
                    table.createGSI(
                        new CreateGlobalSecondaryIndexAction()
                            .withIndexName(indexName)
                            .withKeySchema(indexDefinition.toKeySchemaElements())
                            .withProjection(indexDefinition.toGsiProjection()),
                        attributeDefinitions.get(0));
                  } else if (attributeDefinitions.size() == 2) {
                    table.createGSI(
                        new CreateGlobalSecondaryIndexAction()
                            .withIndexName(indexName)
                            .withKeySchema(indexDefinition.toKeySchemaElements())
                            .withProjection(indexDefinition.toGsiProjection()),
                        attributeDefinitions.get(0),
                        attributeDefinitions.get(1));
                  } else {
                    throw new IllegalStateException("illegal index: " + indexName);
                  }
                  log.info(
                      "Successfully add index: {} on table: {}",
                      indexName,
                      tableDefinition.getTableName());
                }
              });
    }
  }

  public OrmType putItemDeduped(final OrmType object) {
    final List<Expected> dedupeExpected = new ArrayList<>();
    dedupeExpected.add(
        new Expected(getTableDefinition().getHashAndSortKey().getHashKey().getKeyName())
            .notExist());
    if (getTableDefinition().getHashAndSortKey().getRangeKey() != null) {
      dedupeExpected.add(
          new Expected(getTableDefinition().getHashAndSortKey().getRangeKey().getKeyName())
              .notExist());
    }

    final PutItemSpec putItemSpec =
        new PutItemSpec()
            .withItem(ormManager.toDynamoDBItem(object))
            .withExpected(dedupeExpected.toArray(new Expected[0]));

    dynamoDB.getTable(tableDefinition.getTableName()).putItem(putItemSpec).getItem();
    return object;
  }

  public OrmType putItem(final OrmType object) {
    final PutItemSpec putItemSpec = new PutItemSpec().withItem(ormManager.toDynamoDBItem(object));
    dynamoDB.getTable(tableDefinition.getTableName()).putItem(putItemSpec).getItem();
    return object;
  }

  protected OrmType updateItem(
      final Object partitionKeyValue,
      final String updateExpression,
      final String conditionExpression,
      final Map<String, String> nameMap,
      final Map<String, Object> valueMap) {
    Preconditions.checkArgument(
        tableDefinition.getHashAndSortKey().getRangeKey() == null, "rangeKey should be null");
    final UpdateItemSpec updateItemSpec =
        new UpdateItemSpec()
            .withPrimaryKey(
                new PrimaryKey(
                    tableDefinition.getHashAndSortKey().getHashKey().getKeyName(),
                    partitionKeyValue))
            .withUpdateExpression(updateExpression)
            .withConditionExpression(conditionExpression)
            .withNameMap(nameMap)
            .withValueMap(valueMap)
            .withReturnValues("ALL_NEW");
    final UpdateItemOutcome updateItemOutcome =
        dynamoDB.getTable(tableDefinition.getTableName()).updateItem(updateItemSpec);
    return ormManager.toObject(updateItemOutcome.getItem());
  }

  protected OrmType updateItem(
      final Object partitionKeyValue,
      final Object rangeKeyValue,
      final String updateExpression,
      final String conditionExpression,
      final Map<String, String> nameMap,
      final Map<String, Object> valueMap) {
    final UpdateItemSpec updateItemSpec =
        new UpdateItemSpec()
            .withPrimaryKey(
                new PrimaryKey(
                    tableDefinition.getHashAndSortKey().getHashKey().getKeyName(),
                    partitionKeyValue,
                    tableDefinition.getHashAndSortKey().getRangeKey().getKeyName(),
                    rangeKeyValue))
            .withUpdateExpression(updateExpression)
            .withConditionExpression(conditionExpression)
            .withNameMap(nameMap)
            .withValueMap(valueMap)
            .withReturnValues("ALL_NEW");
    final UpdateItemOutcome updateItemOutcome =
        dynamoDB.getTable(tableDefinition.getTableName()).updateItem(updateItemSpec);
    return ormManager.toObject(updateItemOutcome.getItem());
  }

  protected OrmType updateItem(
      final Object partitionKeyValue,
      final Object rangeKeyValue,
      final List<AttributeUpdate> updateAttributes,
      final List<Expected> expectedList) {
    final UpdateItemSpec updateItemSpec =
        new UpdateItemSpec()
            .withPrimaryKey(
                new PrimaryKey(
                    tableDefinition.getHashAndSortKey().getHashKey().getKeyName(),
                    partitionKeyValue,
                    tableDefinition.getHashAndSortKey().getRangeKey().getKeyName(),
                    rangeKeyValue))
            .withAttributeUpdate(updateAttributes)
            .withExpected(expectedList)
            .withReturnValues("ALL_NEW");

    final UpdateItemOutcome updateItemOutcome =
        dynamoDB.getTable(tableDefinition.getTableName()).updateItem(updateItemSpec);
    return ormManager.toObject(updateItemOutcome.getItem());
  }

  protected OrmType updateItem(
      final Object partitionKeyValue,
      final List<AttributeUpdate> updateAttributes,
      final List<Expected> expectedList) {
    Preconditions.checkArgument(
        tableDefinition.getHashAndSortKey().getRangeKey() == null, "rangeKey should be null");
    final UpdateItemSpec updateItemSpec =
        new UpdateItemSpec()
            .withPrimaryKey(
                new PrimaryKey(
                    tableDefinition.getHashAndSortKey().getHashKey().getKeyName(),
                    partitionKeyValue))
            .withAttributeUpdate(updateAttributes)
            .withExpected(expectedList)
            .withReturnValues("ALL_NEW");

    final UpdateItemOutcome updateItemOutcome =
        dynamoDB.getTable(tableDefinition.getTableName()).updateItem(updateItemSpec);
    return ormManager.toObject(updateItemOutcome.getItem());
  }

  public List<BatchWriteItemOutcome> batchPutItem(final List<OrmType> objects) {
    List<BatchWriteItemOutcome> batchWriteItemOutcomes = Lists.newArrayList();
    final List<Item> allItems =
        objects.stream().map(ormManager::toDynamoDBItem).collect(Collectors.toList());
    Iterators.partition(allItems.stream().iterator(), BATCH_WRITE_SIZE)
        .forEachRemaining(
            batch ->
                batchWriteItemOutcomes.add(
                    dynamoDB.batchWriteItem(
                        new TableWriteItems(tableDefinition.getTableName())
                            .withItemsToPut(batch))));

    return batchWriteItemOutcomes;
  }

  public void parallelPutItem(final List<OrmType> objects) {
    final List<List<Item>> items = Lists.newArrayList();
    final List<Item> allItems =
        objects.stream().map(ormManager::toDynamoDBItem).collect(Collectors.toList());
    Iterators.partition(allItems.stream().iterator(), BATCH_WRITE_SIZE)
        .forEachRemaining(batch -> items.add(batch));

    List<Map<String, List<WriteRequest>>> unprocessedItemsList = Lists.newArrayList();

    items.parallelStream()
        .forEach(
            partitionItems -> {
              Map<String, List<WriteRequest>> unprocessedItems =
                  dynamoDB
                      .batchWriteItem(
                          new TableWriteItems(tableDefinition.getTableName())
                              .withItemsToPut(partitionItems))
                      .getUnprocessedItems();
              if (unprocessedItems != null && !unprocessedItems.isEmpty()) {
                unprocessedItemsList.add(unprocessedItems);
              }
            });

    // retry unprocessed items
    int blockTimes = 0;
    while (!CollectionUtils.isEmpty(unprocessedItemsList)) {
      AtomicInteger unprocessedItemsCountBeforeRetry = new AtomicInteger();
      AtomicInteger unprocessedItemsCountAfterRetry = new AtomicInteger();
      List<Map<String, List<WriteRequest>>> currentUnprocessedItemsList = Lists.newArrayList();

      unprocessedItemsList.parallelStream()
          .forEach(
              unprocessedItems -> {
                unprocessedItems
                    .values()
                    .forEach(
                        writeRequests ->
                            unprocessedItemsCountBeforeRetry.addAndGet(writeRequests.size()));
                Map<String, List<WriteRequest>> currentUnprocessedItems =
                    dynamoDB.batchWriteItemUnprocessed(unprocessedItems).getUnprocessedItems();
                if (currentUnprocessedItems != null && !currentUnprocessedItems.isEmpty()) {
                  currentUnprocessedItemsList.add(currentUnprocessedItems);
                  currentUnprocessedItems.values().stream()
                      .forEach(
                          writeRequests ->
                              unprocessedItemsCountAfterRetry.addAndGet(writeRequests.size()));
                }
              });

      if (unprocessedItemsCountAfterRetry.get() < unprocessedItemsCountBeforeRetry.get()) {
        unprocessedItemsList.clear();
        unprocessedItemsList.addAll(currentUnprocessedItemsList);
        blockTimes = 0;
      } else {
        // retry no valid
        blockTimes++;
      }

      // block 3 times, throw exception
      if (blockTimes >= 3) {
        log.error("parallelPutItem failed, block more than 3 times");
        throw new AmazonDynamoDBException("parallelPutItem failed, block more than 3 times");
      }
    }
  }

  public List<OrmType> batchGetItems(final List<PrimaryKey> primaryKeys) {
    if (primaryKeys.isEmpty()) {
      return List.of();
    }
    final BatchGetItemSpec spec =
        new BatchGetItemSpec()
            .withTableKeyAndAttributes(
                new TableKeysAndAttributes(getTableDefinition().getTableName())
                    .withPrimaryKeys(primaryKeys.toArray(new PrimaryKey[0])));
    return batchGetOutcomeToItems(dynamoDB.batchGetItem(spec));
  }

  public OrmType getItem(final Object partitionKeyValue) {
    Preconditions.checkArgument(
        tableDefinition.getHashAndSortKey().getRangeKey() == null, "rangeKey should be null");
    final GetItemSpec spec =
        new GetItemSpec()
            .withPrimaryKey(
                tableDefinition.getHashAndSortKey().getHashKey().getKeyName(), partitionKeyValue);
    return ormManager.toObject(dynamoDB.getTable(tableDefinition.getTableName()).getItem(spec));
  }

  public OrmType getItem(final Object partitionKeyValue, final Object rangeKeyValue) {
    Preconditions.checkArgument(
        tableDefinition.getHashAndSortKey().getRangeKey() != null, "rangeKey should not be null");
    final GetItemSpec spec =
        new GetItemSpec()
            .withPrimaryKey(
                tableDefinition.getHashAndSortKey().getHashKey().getKeyName(),
                partitionKeyValue,
                tableDefinition.getHashAndSortKey().getRangeKey().getKeyName(),
                rangeKeyValue);
    return ormManager.toObject(dynamoDB.getTable(tableDefinition.getTableName()).getItem(spec));
  }

  public OrmType deleteItem(final Object partitionKeyValue) {
    Preconditions.checkArgument(
        tableDefinition.getHashAndSortKey().getRangeKey() == null, "rangeKey should be null");
    final DeleteItemSpec spec =
        new DeleteItemSpec()
            .withPrimaryKey(
                tableDefinition.getHashAndSortKey().getHashKey().getKeyName(), partitionKeyValue);
    return ormManager.toObject(
        dynamoDB.getTable(tableDefinition.getTableName()).deleteItem(spec).getItem());
  }

  public OrmType deleteItem(final Object partitionKeyValue, final Object rangeKeyValue) {
    Preconditions.checkArgument(
        tableDefinition.getHashAndSortKey().getRangeKey() != null, "rangeKey should not be null");
    final DeleteItemSpec spec =
        new DeleteItemSpec()
            .withPrimaryKey(
                tableDefinition.getHashAndSortKey().getHashKey().getKeyName(),
                partitionKeyValue,
                tableDefinition.getHashAndSortKey().getRangeKey().getKeyName(),
                rangeKeyValue);
    return ormManager.toObject(
        dynamoDB.getTable(tableDefinition.getTableName()).deleteItem(spec).getItem());
  }

  public List<OrmType> queryByPartitionKey(
      final Object partitionKeyValue, final QueryFilter... queryFilters) {
    final QuerySpec querySpec =
        new QuerySpec()
            .withHashKey(
                tableDefinition.getHashAndSortKey().getHashKey().getKeyName(), partitionKeyValue)
            .withQueryFilters(queryFilters)
            .withMaxResultSize(LIMIT);

    return queryOutcomeToItems(dynamoDB.getTable(tableDefinition.getTableName()).query(querySpec));
  }

  // pagination based on LastEvaluatedKey
  public PaginationDTO<OrmType> queryPaginateByPartitionKey(
      final Object partitionKeyValue,
      final PrimaryKey exclusiveStartKey,
      final Integer size,
      final QueryFilter... queryFilters) {
    final QuerySpec querySpec =
        new QuerySpec()
            .withHashKey(
                tableDefinition.getHashAndSortKey().getHashKey().getKeyName(), partitionKeyValue)
            .withQueryFilters(queryFilters)
            .withExclusiveStartKey(exclusiveStartKey)
            .withMaxResultSize(size != null ? size : LIMIT);

    return queryOutcomeToPaginationResult(
        dynamoDB.getTable(tableDefinition.getTableName()).query(querySpec));
  }

  public List<OrmType> queryByPartitionKeyWithConsistentRead(
      final Object partitionKeyValue, final QueryFilter... queryFilters) {
    final QuerySpec querySpec =
        new QuerySpec()
            .withHashKey(
                tableDefinition.getHashAndSortKey().getHashKey().getKeyName(), partitionKeyValue)
            .withQueryFilters(queryFilters)
            .withConsistentRead(true)
            .withMaxResultSize(LIMIT);
    return queryOutcomeToItems(dynamoDB.getTable(tableDefinition.getTableName()).query(querySpec));
  }

  private List<OrmType> batchGetOutcomeToItems(final BatchGetItemOutcome outcome) {
    return outcome.getTableItems().get(getTableDefinition().getTableName()).stream()
        .map(ormManager::toObject)
        .collect(Collectors.toList());
  }

  protected List<OrmType> queryOutcomeToItems(final ItemCollection<QueryOutcome> outcomes) {
    final List<OrmType> items = new ArrayList<>();
    for (final Page<Item, QueryOutcome> page : outcomes.pages()) {
      page.iterator().forEachRemaining(item -> items.add(ormManager.toObject(item)));
    }
    return items;
  }

  private PaginationDTO<OrmType> queryOutcomeToPaginationResult(
      final ItemCollection<QueryOutcome> outcomes) {
    final List<OrmType> items = new ArrayList<>();
    for (final Page<Item, QueryOutcome> page : outcomes.pages()) {
      page.iterator().forEachRemaining(item -> items.add(this.getOrmManager().toObject(item)));
    }
    final int totalCount = outcomes.getAccumulatedItemCount();
    final Map<String, AttributeValue> lastEvaluatedKey =
        outcomes.getLastLowLevelResult().getQueryResult().getLastEvaluatedKey();
    return PaginationDTO.<OrmType>builder()
        .items(items)
        .totalCount(totalCount)
        .cursor(lastEvaluatedKey)
        .build();
  }

  private List<OrmType> scanOutcomeToItems(final ItemCollection<ScanOutcome> outcomes) {
    final List<OrmType> items = new ArrayList<>();
    for (final Page<Item, ScanOutcome> page : outcomes.pages()) {
      page.iterator().forEachRemaining(item -> items.add(ormManager.toObject(item)));
    }
    return items;
  }

  private PaginationDTO<OrmType> scanOutcomeToItems(
      final ItemCollection<ScanOutcome> outcomes, final int pageNum) {
    final List<OrmType> items = new ArrayList<>();
    int i = 0;
    for (final Page<Item, ScanOutcome> page : outcomes.pages()) {
      if (pageNum == i) {
        page.iterator().forEachRemaining(item -> items.add(ormManager.toObject(item)));
      }
      i++;
    }
    final int totalCount = outcomes.getAccumulatedItemCount();
    return PaginationDTO.<OrmType>builder().items(items).totalCount(totalCount).build();
  }

  public List<OrmType> queryByRange(
      final Object partitionKeyValue,
      final Object from,
      final Object to,
      final QueryFilter... queryFilters) {
    Preconditions.checkArgument(
        tableDefinition.getHashAndSortKey().getRangeKey() != null,
        "rangeKey is null, so queryByRange() is not supported");

    return queryOutcomeToItems(
        queryByRangeInternal(
            dynamoDB.getTable(tableDefinition.getTableName()),
            partitionKeyValue,
            from,
            to,
            LIMIT,
            queryFilters));
  }

  public List<OrmType> scan() {
    return scanOutcomeToItems(
        scanByRangeInternal(
            dynamoDB.getTable(tableDefinition.getTableName()),
            tableDefinition.getHashAndSortKey(),
            null,
            null));
  }
  
  protected List<OrmType> scan(final ScanSpec scanSpec) {
    return scanOutcomeToItems(dynamoDB.getTable(tableDefinition.getTableName()).scan(scanSpec));
  }

  public List<OrmType> scanWithFilter(final ScanFilter... scanFilters) {
    final ScanApi scanApi = dynamoDB.getTable(tableDefinition.getTableName());
    final ScanSpec scanSpec = new ScanSpec();

    scanSpec.withScanFilters(scanFilters);

    final ItemCollection<ScanOutcome> outcomes = scanApi.scan(scanSpec);
    return scanOutcomeToItems(outcomes);
  }

  public PaginationDTO<OrmType> scanWithFilterPagination(
      final int pageNum, final int pageSize, final ScanFilter... scanFilters) {
    final ScanApi scanApi = dynamoDB.getTable(tableDefinition.getTableName());
    final ScanSpec scanSpec = new ScanSpec();

    scanSpec.withScanFilters(scanFilters).withMaxPageSize(pageSize);

    final ItemCollection<ScanOutcome> outcomes = scanApi.scan(scanSpec);
    return scanOutcomeToItems(outcomes, pageNum);
  }

  public List<OrmType> scanByRange(final Object from, final Object to) {
    Preconditions.checkArgument(
        tableDefinition.getHashAndSortKey().getRangeKey() != null,
        "rangeKey is null, so queryByRange() is not supported");

    return scanOutcomeToItems(
        scanByRangeInternal(
            dynamoDB.getTable(tableDefinition.getTableName()),
            tableDefinition.getHashAndSortKey(),
            from,
            to));
  }

  public List<OrmType> scanOnGsi(final String indexName) {
    return scanOutcomeToItems(
        scanByRangeInternal(
            dynamoDB.getTable(tableDefinition.getTableName()).getIndex(indexName),
            getTableDefinition().getGlobalSecondaryIndices().get(indexName),
            null,
            null));
  }

  public List<OrmType> queryByPartitionKeyOnGsi(
      final String indexName, final Object partitionKeyValue, final QueryFilter... queryFilters) {

    final QuerySpec querySpec =
        new QuerySpec()
            .withHashKey(
                tableDefinition
                    .getGlobalSecondaryIndices()
                    .get(indexName)
                    .getHashKey()
                    .getKeyName(),
                partitionKeyValue)
            .withQueryFilters(queryFilters)
            .withMaxResultSize(LIMIT);

    return queryOutcomeToItems(
        dynamoDB.getTable(tableDefinition.getTableName()).getIndex(indexName).query(querySpec));
  }

  public List<OrmType> queryByPartitionKeyAndSortKeyOnGsi(
      final String indexName,
      final Object partitionKeyValue,
      final RangeKeyCondition rangeKeyCondition,
      final QueryFilter... queryFilters) {
    Preconditions.checkArgument(
        tableDefinition.getGlobalSecondaryIndices().get(indexName).getRangeKey() != null,
        "the index %s rangeKey is null, so queryByPartitionKeyAndSortKeyOnGsi() is not supported",
        indexName);
    final QuerySpec querySpec =
        new QuerySpec()
            .withHashKey(
                tableDefinition
                    .getGlobalSecondaryIndices()
                    .get(indexName)
                    .getHashKey()
                    .getKeyName(),
                partitionKeyValue)
            .withRangeKeyCondition(rangeKeyCondition)
            .withQueryFilters(queryFilters)
            .withMaxResultSize(LIMIT);

    return queryOutcomeToItems(
        dynamoDB.getTable(tableDefinition.getTableName()).getIndex(indexName).query(querySpec));
  }

  public Integer queryCountByPartitionKey(
      final Object partitionKeyValue, final QueryFilter... queryFilters) {
    final QuerySpec querySpec =
        new QuerySpec()
            .withHashKey(
                tableDefinition.getHashAndSortKey().getHashKey().getKeyName(), partitionKeyValue)
            .withQueryFilters(queryFilters)
            .withSelect(Select.COUNT);

    final ItemCollection<QueryOutcome> query =
        dynamoDB.getTable(tableDefinition.getTableName()).query(querySpec);
    while (query.iterator().hasNext()) {}
    return query.getAccumulatedItemCount();
  }

  public Integer queryCountByPartitionKeyOnGsi(
      final String indexName, final Object partitionKeyValue, final QueryFilter... queryFilters) {
    final QuerySpec querySpec =
        new QuerySpec()
            .withHashKey(
                tableDefinition
                    .getGlobalSecondaryIndices()
                    .get(indexName)
                    .getHashKey()
                    .getKeyName(),
                partitionKeyValue)
            .withQueryFilters(queryFilters)
            .withSelect(Select.COUNT);

    final ItemCollection<QueryOutcome> query =
        dynamoDB.getTable(tableDefinition.getTableName()).getIndex(indexName).query(querySpec);
    while (query.iterator().hasNext()) {}
    return query.getAccumulatedItemCount();
  }

  /**
   * TODO this function by defualt use ALL projection on GSI, will need to consider add more
   * flexibility
   */
  public List<OrmType> queryByRangeOnGsi(
      final String indexName,
      final Object partitionKeyValue,
      final Object from,
      final Object to,
      final QueryFilter... queryFilters) {
    Preconditions.checkArgument(
        tableDefinition.getGlobalSecondaryIndices().get(indexName).getRangeKey() != null,
        "rangeKey is null, so queryByRange() is not supported");

    return queryOutcomeToItems(
        queryByRangeInternal(
            dynamoDB.getTable(tableDefinition.getTableName()).getIndex(indexName),
            partitionKeyValue,
            from,
            to,
            LIMIT,
            queryFilters));
  }

  protected ItemCollection<QueryOutcome> queryByRangeInternal(
      final QueryApi queryApi,
      final Object partitionKeyValue,
      final Object from,
      final Object to,
      final int limit,
      final QueryFilter... queryFilters) {

    final RangeKeyCondition rangeKeyCondition =
        new RangeKeyCondition(tableDefinition.getHashAndSortKey().getRangeKey().getKeyName());
    final QuerySpec querySpec;
    if (from == null && to == null) {
      querySpec =
          new QuerySpec()
              .withHashKey(
                  tableDefinition.getHashAndSortKey().getHashKey().getKeyName(), partitionKeyValue)
              .withQueryFilters(queryFilters)
              .withMaxResultSize(limit);
    } else {
      if (from == null) {
        rangeKeyCondition.le(to);
      } else if (to == null) {
        rangeKeyCondition.ge(from);
      } else {
        rangeKeyCondition.between(from, to);
      }
      querySpec =
          new QuerySpec()
              .withHashKey(
                  tableDefinition.getHashAndSortKey().getHashKey().getKeyName(), partitionKeyValue)
              .withRangeKeyCondition(rangeKeyCondition)
              .withQueryFilters(queryFilters)
              .withMaxResultSize(limit);
    }

    return queryApi.query(querySpec);
  }

  protected ItemCollection<ScanOutcome> scanByRangeInternal(
      final ScanApi scanApi,
      final IndexDefinition indexDefinition,
      final Object from,
      final Object to) {

    ScanFilter scanFilter = null;
    if (from != null || to != null) {
      scanFilter = new ScanFilter(indexDefinition.getRangeKey().getKeyName());
      if (from == null) {
        scanFilter.le(to);
      } else if (to == null) {
        scanFilter.ge(from);
      } else {
        scanFilter.between(from, to);
      }
    }

    final ScanSpec scanSpec = new ScanSpec();
    if (scanFilter != null) {
      scanSpec.withScanFilters(scanFilter);
    }

    return scanApi.scan(scanSpec);
  }
}
