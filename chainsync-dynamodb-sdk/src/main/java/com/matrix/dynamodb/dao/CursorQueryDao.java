package com.matrix.dynamodb.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.Page;
import com.amazonaws.services.dynamodbv2.document.QueryFilter;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.api.QueryApi;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.fasterxml.jackson.databind.JsonNode;
import com.matrix.dynamodb.model.CursorPageQueryResult;
import com.matrix.dynamodb.model.CursorPageQuerySpec;
import com.matrix.dynamodb.model.CursorQueryResult;
import com.matrix.dynamodb.model.CursorQuerySpec;
import com.matrix.dynamodb.model.CursorQuerySpec.CursorQuerySpecBuilder;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
import com.matrix.dynamodb.orm.dynamo.KeyDefinition;
import com.matrix.dynamodb.util.SerdeUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.CollectionUtils;

/**
 * @author reimia
 */
public class CursorQueryDao<OrmType> extends BaseQueryDao<OrmType> {

  public CursorQueryDao(
      final DynamoDBTableOrmManager<OrmType> ormManager, final DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }

  /** can only query next page */
  public CursorQueryResult<OrmType> queryByCursor(
      final Object partitionKeyValue,
      final CursorQuerySpec cursorQuerySpec,
      final QueryFilter... queryFilters) {
    return queryByCursorWithRangeCondition(
        null, partitionKeyValue, cursorQuerySpec, null, queryFilters);
  }

  public CursorQueryResult<OrmType> queryByCursor(
      final String indexName,
      final Object partitionKeyValue,
      final CursorQuerySpec cursorQuerySpec,
      final QueryFilter... queryFilters) {
    return queryByCursorWithRangeCondition(
        indexName, partitionKeyValue, cursorQuerySpec, null, queryFilters);
  }

  public CursorQueryResult<OrmType> queryByCursorWithRangeCondition(
      final Object partitionKeyValue,
      final CursorQuerySpec cursorQuerySpec,
      final RangeKeyCondition rangeKeyCondition,
      final QueryFilter... queryFilters) {
    return queryByCursorWithRangeCondition(
        null, partitionKeyValue, cursorQuerySpec, rangeKeyCondition, queryFilters);
  }

  public CursorPageQueryResult<OrmType> queryByCursorPage(
      final Object partitionKeyValue,
      final CursorPageQuerySpec cursorPageQuerySpec,
      final QueryFilter... queryFilters) {
    return queryByCursorPage(partitionKeyValue, cursorPageQuerySpec, null, queryFilters);
  }

  /**
   * can query both pre and next page
   *
   * <p>when query next page, use next key and ascending
   *
   * <p>when query prev page, use prev key and descending
   *
   * <p>this method's core is to query limit + 1 count data to judge query should have nextKey and
   * PrevKey
   */
  public CursorPageQueryResult<OrmType> queryByCursorPage(
      final Object partitionKeyValue,
      final CursorPageQuerySpec cursorPageQuerySpec,
      final RangeKeyCondition rangeKeyCondition,
      final QueryFilter... queryFilters) {
    final boolean isNextPage = cursorPageQuerySpec.isNextPage();
    final boolean ascending = cursorPageQuerySpec.isNextPage() == cursorPageQuerySpec.isAscending();
    final boolean isFirstQuery =
        CollectionUtils.isEmpty(cursorPageQuerySpec.getPrevKey())
            && CollectionUtils.isEmpty(cursorPageQuerySpec.getNextKey());
    final CursorQuerySpec querySpec;
    final CursorQuerySpecBuilder builder =
        CursorQuerySpec.builder().limit(cursorPageQuerySpec.getLimit() + 1).ascending(ascending);
    if (isFirstQuery) {
      querySpec = builder.build();
    } else {
      // use different key for query nextPage or prevPage
      querySpec =
          isNextPage
              ? builder.exclusiveStartKey(cursorPageQuerySpec.getNextKey()).build()
              : builder.exclusiveStartKey(cursorPageQuerySpec.getPrevKey()).build();
    }

    final CursorQueryResult<OrmType> result =
        queryByCursorWithRangeCondition(
            partitionKeyValue, querySpec, rangeKeyCondition, queryFilters);
    final List<OrmType> items = result.getItems();
    if (!isNextPage) {
      // item should always be same order
      Collections.reverse(items);
    }

    final Map<String, Object> prevCursorMap = new HashMap<>();
    final Map<String, Object> nextCursorMap = new HashMap<>();

    // if query size max than limit size, should have prevKey and LastKey
    if (items.size() > cursorPageQuerySpec.getLimit()) {
      List<OrmType> needItems = items.subList(0, cursorPageQuerySpec.getLimit());
      if (!isNextPage) {
        needItems = items.subList(1, cursorPageQuerySpec.getLimit() + 1);
      }
      final JsonNode prevJsonNode = SerdeUtil.DEFAULT_OBJECT_MAPPER.valueToTree(needItems.get(0));
      final JsonNode nextJsonNode =
          SerdeUtil.DEFAULT_OBJECT_MAPPER.valueToTree(
              needItems.get(cursorPageQuerySpec.getLimit() - 1));
      prevCursorMap.putAll(
          convertKeyToMap(prevJsonNode, tableDefinition.getHashAndSortKey().getHashKey()));
      prevCursorMap.putAll(
          convertKeyToMap(prevJsonNode, tableDefinition.getHashAndSortKey().getRangeKey()));
      nextCursorMap.putAll(
          convertKeyToMap(nextJsonNode, tableDefinition.getHashAndSortKey().getHashKey()));
      nextCursorMap.putAll(
          convertKeyToMap(nextJsonNode, tableDefinition.getHashAndSortKey().getRangeKey()));

      // if first query, no prevKey
      if (isFirstQuery) {
        prevCursorMap.clear();
      }

      return new CursorPageQueryResult<>(needItems, prevCursorMap, nextCursorMap);
    }

    // if query size less than limit size and it is first query, means all item has been queryed,
    // no need prevKey and nextKey
    if (isFirstQuery) {
      return new CursorPageQueryResult<>(items, prevCursorMap, nextCursorMap);
    }

    // if query size less than limit size and is query nextPage, means this is the last page
    if (isNextPage) {
      final JsonNode prevJsonNode = SerdeUtil.DEFAULT_OBJECT_MAPPER.valueToTree(items.get(0));
      prevCursorMap.putAll(
          convertKeyToMap(prevJsonNode, tableDefinition.getHashAndSortKey().getHashKey()));
      prevCursorMap.putAll(
          convertKeyToMap(prevJsonNode, tableDefinition.getHashAndSortKey().getRangeKey()));
      return new CursorPageQueryResult<>(items, prevCursorMap, nextCursorMap);
    } else {
      // if query size less than limit size and is not query nextPage, means this is the first page
      final JsonNode nextJsonNode =
          SerdeUtil.DEFAULT_OBJECT_MAPPER.valueToTree(items.get(items.size() - 1));
      nextCursorMap.putAll(
          convertKeyToMap(nextJsonNode, tableDefinition.getHashAndSortKey().getHashKey()));
      nextCursorMap.putAll(
          convertKeyToMap(nextJsonNode, tableDefinition.getHashAndSortKey().getRangeKey()));
      return new CursorPageQueryResult<>(items, prevCursorMap, nextCursorMap);
    }
  }

  /**
   * Query by cursor base method
   *
   * @param indexName query on GSI name
   * @param partitionKeyValue GSI pk value
   * @param cursorQuerySpec cursor query stuff (ExclusiveStartKey, limit, direction)
   * @param rangeKeyCondition range key condition
   * @param queryFilters query filters
   * @return cursor query result
   */
  public CursorQueryResult<OrmType> queryByCursorWithRangeCondition(
      final String indexName,
      final Object partitionKeyValue,
      final CursorQuerySpec cursorQuerySpec,
      final RangeKeyCondition rangeKeyCondition,
      final QueryFilter... queryFilters) {

    final QuerySpec querySpec =
        new QuerySpec()
            .withScanIndexForward(cursorQuerySpec.isAscending())
            .withMaxPageSize(cursorQuerySpec.getLimit())
            .withMaxResultSize(cursorQuerySpec.getLimit());

    // range key condition
    if (rangeKeyCondition != null) {
      querySpec.withRangeKeyCondition(rangeKeyCondition);
    }

    // cursor
    if (cursorQuerySpec.getExclusiveStartKey() != null
        && !cursorQuerySpec.getExclusiveStartKey().isEmpty()) {
      querySpec.withExclusiveStartKey(cursorQuerySpec.exclusiveStartKeyAttributes());
    }

    final QueryApi queryApi =
        indexName == null
            ? getDynamoDB().getTable(tableDefinition.getTableName())
            : getDynamoDB().getTable(tableDefinition.getTableName()).getIndex(indexName);

    final ItemCollection<QueryOutcome> outcome =
        query(queryApi, partitionKeyValue, querySpec, queryFilters);

    return outcomeToCursorQueryResult(outcome);
  }

  protected ItemCollection<QueryOutcome> query(
      final QueryApi queryApi,
      final Object partitionKeyValue,
      final QuerySpec querySpec,
      final QueryFilter... queryFilters) {
    if (queryFilters != null && queryFilters.length != 0) {
      querySpec.withQueryFilters(queryFilters);
    }
    if (queryApi instanceof Table) {
      querySpec.withHashKey(
          tableDefinition.getHashAndSortKey().getHashKey().getKeyName(), partitionKeyValue);
    } else if (queryApi instanceof Index) {
      final String indexName = ((Index) queryApi).getIndexName();
      querySpec.withHashKey(
          tableDefinition.getGlobalSecondaryIndices().get(indexName).getHashKey().getKeyName(),
          partitionKeyValue);
    } else {
      throw new IllegalStateException("queryApi type not supporting!");
    }
    return queryApi.query(querySpec);
  }

  private CursorQueryResult<OrmType> outcomeToCursorQueryResult(
      final ItemCollection<QueryOutcome> outcome) {
    final List<OrmType> items = new ArrayList<>();
    for (final Page<Item, QueryOutcome> page : outcome.pages()) {
      page.iterator().forEachRemaining(item -> items.add(getOrmManager().toObject(item)));
    }
    final Map<String, AttributeValue> lastEvaluatedKey =
        CollectionUtils.isEmpty(items)
            ? null
            : outcome.getLastLowLevelResult().getQueryResult().getLastEvaluatedKey();
    return new CursorQueryResult<>(items, convertAttributeValueToObject(lastEvaluatedKey));
  }

  private Map<String, Object> convertAttributeValueToObject(final Map<String, AttributeValue> map) {
    if (map == null) {
      return Map.of();
    }
    final Map<String, Object> result = new HashMap<>();
    map.forEach(
        (key, value) -> {
          if (value.getS() != null) {
            result.put(key, value.getS());
          } else if (value.getN() != null) {
            result.put(key, new BigDecimal(value.getN()));
          } else if (value.getB() != null) {
            result.put(key, value.getB());
          } else if (value.getNULL() != null) {
            result.put(key, null);
          } else {
            throw new IllegalStateException("Unsupported AttributeValue type!");
          }
        });
    return result;
  }

  private Map<String, Object> convertKeyToMap(final JsonNode jsonNode, final KeyDefinition key) {
    final Map<String, Object> map = new HashMap<>();
    if (key.getKeyAttributeType().equals(ScalarAttributeType.S)) {
      map.put(key.getKeyName(), jsonNode.get(key.getKeyName()).asText());
    } else if (key.getKeyAttributeType().equals(ScalarAttributeType.N)) {
      map.put(key.getKeyName(), new BigDecimal(jsonNode.get(key.getKeyName()).asText()));
    } else {
      throw new UnsupportedOperationException("unsupported key definition attr type");
    }
    return map;
  }
}
