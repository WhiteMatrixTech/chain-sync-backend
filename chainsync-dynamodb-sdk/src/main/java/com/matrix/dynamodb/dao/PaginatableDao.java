package com.matrix.dynamodb.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.Page;
import com.amazonaws.services.dynamodbv2.document.QueryFilter;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.google.common.base.Preconditions;
import com.matrix.dynamodb.model.PaginatedQueryResult;
import com.matrix.dynamodb.model.PaginatedQuerySpec;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/** @author shuyizhang */
public abstract class PaginatableDao<OrmType, P> extends BaseQueryDao<OrmType> {

  private static final int QUERY_LIMIT = 10000;
  private static final int BATCH_SIZE = 1000;
  private final java.util.function.Function<OrmType, P> paginatorExtractor;
  private final Comparator<P> comparator;

  public PaginatableDao(
      final Function<OrmType, P> paginatorExtractor,
      final Comparator<P> comparator,
      final DynamoDBTableOrmManager<OrmType> ormManager,
      final DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
    this.paginatorExtractor = paginatorExtractor;
    this.comparator = comparator;
  }

  // TODO add limit later
  public PaginatedQueryResult<OrmType, P> queryPaginatedResult(
      final Object partitionKeyValue,
      final PaginatedQuerySpec<P> paginatedQuerySpec,
      final QueryFilter... queryFilters) {
    P from = paginatedQuerySpec.getFrom();
    boolean fromExclusive = false;
    if (from == null) {
      if (paginatedQuerySpec.getPaginator() != null) {
        fromExclusive = true;
        from = paginatedQuerySpec.getPaginator();
        // paginator is not null
        if (paginatedQuerySpec.getTo() != null) {
          Preconditions.checkArgument(
              comparator.compare(from, paginatedQuerySpec.getTo()) <= 0,
              "Paginator should be less than or equals to To");
        }
      }
    } else {
      if (paginatedQuerySpec.getTo() != null) {
        Preconditions.checkArgument(
            comparator.compare(from, paginatedQuerySpec.getTo()) <= 0,
            "From should be less than or equals to To");
      }
      // from is not null
      if (paginatedQuerySpec.getPaginator() != null) {
        if (comparator.compare(from, paginatedQuerySpec.getPaginator()) <= 0) {
          // paginator >= from, then use paginator as from
          fromExclusive = true;
          from = paginatedQuerySpec.getPaginator();
        }
      }
    }

    return this.queryByRangeV2(
        partitionKeyValue,
        from,
        paginatedQuerySpec.getTo(),
        fromExclusive,
        paginatedQuerySpec.getLimit() != null ? paginatedQuerySpec.getLimit() : QUERY_LIMIT,
        queryFilters);
  }

  public PaginatedQueryResult<OrmType, P> queryByRangeV2(
      final Object partitionKeyValue,
      final P from,
      final P to,
      final boolean fromExclusive,
      final int queryLimit,
      final QueryFilter... queryFilters) {
    Preconditions.checkArgument(
        tableDefinition.getHashAndSortKey().getRangeKey() != null,
        "rangeKey is null, so queryByRange() is not supported");

    final List<OrmType> items = new ArrayList<>();
    P currentFrom = from;

    do {
      final P keyToExclude = !items.isEmpty() || fromExclusive ? currentFrom : null;
      final int currentBatch =
          Math.min(queryLimit - items.size(), BATCH_SIZE) + (null != keyToExclude ? 1 : 0);
      final ItemCollection<QueryOutcome> outcomes =
          queryByRangeInternal(
              getDynamoDB().getTable(tableDefinition.getTableName()),
              partitionKeyValue,
              currentFrom,
              to,
              currentBatch,
              queryFilters);

      final AtomicReference<OrmType> maxItem = new AtomicReference<>(null);
      for (final Page<Item, QueryOutcome> page : outcomes.pages()) {
        page.iterator()
            .forEachRemaining(
                item -> {
                  final OrmType ormItem = getOrmManager().toObject(item);
                  if (keyToExclude != null
                      && keyToExclude.equals(paginatorExtractor.apply(ormItem))) {
                    return;
                  }
                  items.add(ormItem);
                  final OrmType currentMax = maxItem.get();
                  if (currentMax == null
                      || comparator.compare(
                              paginatorExtractor.apply(currentMax),
                              paginatorExtractor.apply(ormItem))
                          < 0) {
                    maxItem.set(ormItem);
                  }
                });
      }
      currentFrom = maxItem.get() != null ? paginatorExtractor.apply(maxItem.get()) : null;
    } while (items.size() < queryLimit && currentFrom != null);
    return new PaginatedQueryResult<>(items, currentFrom);
  }
}
