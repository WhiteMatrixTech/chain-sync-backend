package com.chainsync.dynamodb.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.QueryFilter;
import com.chainsync.dynamodb.annotation.DynamoAttribute;
import com.chainsync.dynamodb.annotation.DynamoGSIKey;
import com.chainsync.dynamodb.annotation.DynamoKey;
import com.chainsync.dynamodb.annotation.DynamoTable;
import com.chainsync.dynamodb.model.CursorPageQueryResult;
import com.chainsync.dynamodb.model.CursorPageQuerySpec;
import com.chainsync.dynamodb.model.CursorQueryResult;
import com.chainsync.dynamodb.model.CursorQuerySpec;
import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
import com.chainsync.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;
import com.chainsync.dynamodb.util.DynamoDBLocalProvider;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author reimia
 */
class CursorQueryDaoTest {

  private static final String PK = "pk";
  private static final String RK = "rk";
  private static final String ATTR = "attr";
  private static final String INDEX_RK = "index_rk";

  private static final List<TestObject> testObjects = new ArrayList<>();
  private static TestQueryDao dao;

  static {
    final String[] pks = new String[] {"a", "a", "a", "b", "b", "b", "c", "c", "c", "d", "d", "d"};
    final Integer[] rks = new Integer[] {1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3};
    final String[] attrs =
        new String[] {"x", "x", "y", "y", "x", "x", "y", "y", "x", "x", "y", "y"};
    for (int i = 0; i < 12; i++) {
      testObjects.add(new TestObject(pks[i], rks[i], attrs[i]));
    }
  }

  @BeforeAll
  public static void setup() {
    final DynamoDB dynamoDB = new DynamoDB(DynamoDBLocalProvider.getInstance());
    final DynamoDBTableOrmManager<TestObject> ormManager =
        new AnnotatedDynamoDBTableOrmManager<>("cursor-query-test-table", TestObject.class);
    dao = new TestQueryDao(ormManager, dynamoDB);
    dao.batchPutItem(testObjects);
  }

  @AfterAll
  public static void tearDown() {
    DynamoDBLocalProvider.tearDown();
  }

  @Test
  void queryByCursor_limit2_should_firstReturn01_then2() {
    final CursorQueryResult<TestObject> queryResult1 =
        dao.queryByCursor("a", CursorQuerySpec.builder().limit(2).ascending(true).build());
    Assertions.assertEquals(
        List.of(testObjects.get(0), testObjects.get(1)), queryResult1.getItems());

    Assertions.assertEquals(
        Map.of("pk", "a", "rk", new BigDecimal(2)), queryResult1.getLastEvaluatedKey());

    final CursorQueryResult<TestObject> queryResult2 =
        dao.queryByCursor(
            "a",
            CursorQuerySpec.builder()
                .limit(2)
                .exclusiveStartKey(queryResult1.getLastEvaluatedKey())
                .ascending(true)
                .build());
    Assertions.assertEquals(List.of(testObjects.get(2)), queryResult2.getItems());
    Assertions.assertEquals(Map.of(), queryResult2.getLastEvaluatedKey());
  }

  @Test
  void queryByCursor_limit2_orderFalse_should_firstReturn21_then0() {
    final CursorQueryResult<TestObject> queryResult1 =
        dao.queryByCursor("a", CursorQuerySpec.builder().limit(2).ascending(false).build());
    Assertions.assertEquals(
        List.of(testObjects.get(2), testObjects.get(1)), queryResult1.getItems());
    Assertions.assertEquals(
        Map.of("pk", "a", "rk", new BigDecimal(2)), queryResult1.getLastEvaluatedKey());

    final CursorQueryResult<TestObject> queryResult2 =
        dao.queryByCursor(
            "a",
            CursorQuerySpec.builder()
                .limit(2)
                .exclusiveStartKey(queryResult1.getLastEvaluatedKey())
                .ascending(false)
                .build());
    Assertions.assertEquals(List.of(testObjects.get(0)), queryResult2.getItems());
    Assertions.assertEquals(Map.of(), queryResult2.getLastEvaluatedKey());
  }

  @Test
  void queryByCursor_limit3_should_return123_with3LastEvaluatedKey() {
    final CursorQueryResult<TestObject> queryResult1 =
        dao.queryByCursor("a", CursorQuerySpec.builder().limit(3).ascending(true).build());
    Assertions.assertEquals(
        List.of(testObjects.get(0), testObjects.get(1), testObjects.get(2)),
        queryResult1.getItems());
    Assertions.assertEquals(
        Map.of("pk", "a", "rk", new BigDecimal(3)), queryResult1.getLastEvaluatedKey());
  }

  @Test
  void queryByCursor_limit4_should_return123_withEmptyLastEvaluatedKey() {
    final CursorQueryResult<TestObject> queryResult1 =
        dao.queryByCursor("a", CursorQuerySpec.builder().limit(4).ascending(true).build());
    Assertions.assertEquals(
        List.of(testObjects.get(0), testObjects.get(1), testObjects.get(2)),
        queryResult1.getItems());
    Assertions.assertEquals(Map.of(), queryResult1.getLastEvaluatedKey());
  }

  @Test
  void queryByCursor_withFilter() {
    final CursorQueryResult<TestObject> queryResult1 =
        dao.queryByCursor(
            "a",
            CursorQuerySpec.builder().limit(2).ascending(true).build(),
            new QueryFilter("attr").in("x"));
    Assertions.assertEquals(
        List.of(testObjects.get(0), testObjects.get(1)), queryResult1.getItems());
    Assertions.assertEquals(
        Map.of("pk", "a", "rk", new BigDecimal(2)), queryResult1.getLastEvaluatedKey());

    final CursorQueryResult<TestObject> queryResult2 =
        dao.queryByCursor(
            "a",
            CursorQuerySpec.builder()
                .limit(2)
                .exclusiveStartKey(queryResult1.getLastEvaluatedKey())
                .ascending(true)
                .build(),
            new QueryFilter("attr").in("x"));
    Assertions.assertEquals(List.of(), queryResult2.getItems());
    Assertions.assertEquals(Map.of(), queryResult2.getLastEvaluatedKey());
  }

  @Test
  void queryByCursor_onGSI_limit3_should_firstReturn036_then9() {
    final CursorQueryResult<TestObject> queryResult1 =
        dao.queryByCursor(INDEX_RK, 1, CursorQuerySpec.builder().limit(3).ascending(true).build());
    Assertions.assertEquals(
        List.of(testObjects.get(0), testObjects.get(3), testObjects.get(6)),
        queryResult1.getItems());
    Assertions.assertEquals(
        Map.of("pk", "c", "rk", new BigDecimal(1)), queryResult1.getLastEvaluatedKey());

    final CursorQueryResult<TestObject> queryResult2 =
        dao.queryByCursor(
            INDEX_RK,
            1,
            CursorQuerySpec.builder()
                .limit(2)
                .exclusiveStartKey(queryResult1.getLastEvaluatedKey())
                .ascending(true)
                .build());
    Assertions.assertEquals(List.of(testObjects.get(9)), queryResult2.getItems());
    Assertions.assertEquals(Map.of(), queryResult2.getLastEvaluatedKey());
  }

  @Test
  void queryByCursorPage_limit10_should_return123_withEmptyPrevKeyAndNextKey() {
    final CursorPageQueryResult<TestObject> queryResult =
        dao.queryByCursorPage(
            "a", CursorPageQuerySpec.builder().limit(10).nextPage(true).ascending(true).build());
    Assertions.assertTrue(queryResult.getNextKey().isEmpty());
    Assertions.assertTrue(queryResult.getPrevKey().isEmpty());
    Assertions.assertEquals(
        List.of(testObjects.get(0), testObjects.get(1), testObjects.get(2)),
        queryResult.getItems());
  }

  @Test
  void queryByCursorPage_limit10_should_return123_withEmptyPrevKeyAndNextKey_ascendingFalse() {
    final CursorPageQueryResult<TestObject> queryResult =
        dao.queryByCursorPage(
            "a", CursorPageQuerySpec.builder().limit(10).nextPage(true).ascending(false).build());
    Assertions.assertTrue(queryResult.getNextKey().isEmpty());
    Assertions.assertTrue(queryResult.getPrevKey().isEmpty());
    Assertions.assertEquals(
        List.of(testObjects.get(2), testObjects.get(1), testObjects.get(0)),
        queryResult.getItems());
  }

  @Test
  void queryByCursorPage_queryNextPage_thenPrevPage_thenNextPage() {
    final CursorPageQueryResult<TestObject> queryResult1 =
        dao.queryByCursorPage(
            "a", CursorPageQuerySpec.builder().limit(2).nextPage(true).ascending(true).build());
    Assertions.assertFalse(queryResult1.getNextKey().isEmpty());
    Assertions.assertTrue(queryResult1.getPrevKey().isEmpty());
    Assertions.assertEquals(
        List.of(testObjects.get(0), testObjects.get(1)), queryResult1.getItems());

    final CursorPageQueryResult<TestObject> queryResult2 =
        dao.queryByCursorPage(
            "a",
            CursorPageQuerySpec.builder()
                .nextKey(queryResult1.getNextKey())
                .prevKey(queryResult1.getPrevKey())
                .limit(2)
                .nextPage(true)
                .ascending(true)
                .build());
    Assertions.assertTrue(queryResult2.getNextKey().isEmpty());
    Assertions.assertFalse(queryResult2.getPrevKey().isEmpty());
    Assertions.assertEquals(List.of(testObjects.get(2)), queryResult2.getItems());

    final CursorPageQueryResult<TestObject> queryResult3 =
        dao.queryByCursorPage(
            "a",
            CursorPageQuerySpec.builder()
                .nextKey(queryResult2.getNextKey())
                .prevKey(queryResult2.getPrevKey())
                .limit(2)
                .nextPage(false)
                .ascending(true)
                .build());
    Assertions.assertFalse(queryResult3.getNextKey().isEmpty());
    Assertions.assertTrue(queryResult3.getPrevKey().isEmpty());
    Assertions.assertEquals(
        List.of(testObjects.get(0), testObjects.get(1)), queryResult3.getItems());

    Assertions.assertEquals(queryResult1, queryResult3);
  }

  @Test
  void queryByCursorPage_queryNextPage_thenPrevPage_thenNextPage_ascendingFalse() {
    final CursorPageQueryResult<TestObject> queryResult1 =
        dao.queryByCursorPage(
            "a", CursorPageQuerySpec.builder().limit(2).nextPage(true).ascending(false).build());
    Assertions.assertFalse(queryResult1.getNextKey().isEmpty());
    Assertions.assertTrue(queryResult1.getPrevKey().isEmpty());
    Assertions.assertEquals(
        List.of(testObjects.get(2), testObjects.get(1)), queryResult1.getItems());

    final CursorPageQueryResult<TestObject> queryResult2 =
        dao.queryByCursorPage(
            "a",
            CursorPageQuerySpec.builder()
                .nextKey(queryResult1.getNextKey())
                .prevKey(queryResult1.getPrevKey())
                .limit(2)
                .nextPage(true)
                .ascending(false)
                .build());
    Assertions.assertTrue(queryResult2.getNextKey().isEmpty());
    Assertions.assertFalse(queryResult2.getPrevKey().isEmpty());
    Assertions.assertEquals(List.of(testObjects.get(0)), queryResult2.getItems());

    final CursorPageQueryResult<TestObject> queryResult3 =
        dao.queryByCursorPage(
            "a",
            CursorPageQuerySpec.builder()
                .nextKey(queryResult2.getNextKey())
                .prevKey(queryResult2.getPrevKey())
                .limit(2)
                .nextPage(false)
                .ascending(false)
                .build());
    Assertions.assertFalse(queryResult3.getNextKey().isEmpty());
    Assertions.assertTrue(queryResult3.getPrevKey().isEmpty());
    Assertions.assertEquals(
        List.of(testObjects.get(2), testObjects.get(1)), queryResult3.getItems());

    Assertions.assertEquals(queryResult1, queryResult3);
  }

  @Test
  void queryByCursorPage_queryNextPage_thenNextPage_thenNextPage_thenPrevPage_ascendingFalse() {
    final CursorPageQueryResult<TestObject> queryResult1 =
        dao.queryByCursorPage(
            "a", CursorPageQuerySpec.builder().limit(1).nextPage(true).ascending(false).build());
    Assertions.assertFalse(queryResult1.getNextKey().isEmpty());
    Assertions.assertTrue(queryResult1.getPrevKey().isEmpty());
    Assertions.assertEquals(List.of(testObjects.get(2)), queryResult1.getItems());

    final CursorPageQueryResult<TestObject> queryResult2 =
        dao.queryByCursorPage(
            "a",
            CursorPageQuerySpec.builder()
                .nextKey(queryResult1.getNextKey())
                .prevKey(queryResult1.getPrevKey())
                .limit(1)
                .nextPage(true)
                .ascending(false)
                .build());
    Assertions.assertFalse(queryResult2.getNextKey().isEmpty());
    Assertions.assertFalse(queryResult2.getPrevKey().isEmpty());
    Assertions.assertEquals(List.of(testObjects.get(1)), queryResult2.getItems());

    final CursorPageQueryResult<TestObject> queryResult3 =
        dao.queryByCursorPage(
            "a",
            CursorPageQuerySpec.builder()
                .nextKey(queryResult2.getNextKey())
                .prevKey(queryResult2.getPrevKey())
                .limit(1)
                .nextPage(true)
                .ascending(false)
                .build());
    Assertions.assertTrue(queryResult3.getNextKey().isEmpty());
    Assertions.assertFalse(queryResult3.getPrevKey().isEmpty());
    Assertions.assertEquals(List.of(testObjects.get(0)), queryResult3.getItems());

    final CursorPageQueryResult<TestObject> queryResult4 =
        dao.queryByCursorPage(
            "a",
            CursorPageQuerySpec.builder()
                .nextKey(queryResult3.getNextKey())
                .prevKey(queryResult3.getPrevKey())
                .limit(1)
                .nextPage(false)
                .ascending(false)
                .build());
    Assertions.assertFalse(queryResult4.getNextKey().isEmpty());
    Assertions.assertFalse(queryResult4.getPrevKey().isEmpty());
    Assertions.assertEquals(List.of(testObjects.get(1)), queryResult4.getItems());

    Assertions.assertEquals(queryResult2, queryResult4);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder(toBuilder = true)
  @DynamoTable(globalSecondaryIndices = {INDEX_RK})
  public static class TestObject {

    @DynamoKey
    @DynamoGSIKey(
        dynamoGSINames = {INDEX_RK},
        dynamoKeyType = DynamoGSIKey.RANGE)
    @DynamoAttribute(attributeName = PK)
    private String pk;

    @DynamoKey(dynamoKeyType = DynamoKey.RANGE)
    @DynamoGSIKey(dynamoGSINames = {INDEX_RK})
    @DynamoAttribute(attributeName = RK, attributeType = "N")
    private Integer rk;

    @DynamoAttribute(attributeName = ATTR)
    private String attr;
  }

  static class TestQueryDao extends CursorQueryDao<TestObject> {

    public TestQueryDao(
        @NonNull final DynamoDBTableOrmManager<TestObject> ormManager,
        @NonNull final DynamoDB dynamoDB) {
      super(ormManager, dynamoDB);
    }
  }
}
