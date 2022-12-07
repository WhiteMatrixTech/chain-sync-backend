package com.chainsync.dynamodb.dao;

import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition;
import com.amazonaws.services.dynamodbv2.document.ScanFilter;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.google.common.collect.ImmutableMap;
import com.chainsync.dynamodb.annotation.DynamoAttribute;
import com.chainsync.dynamodb.annotation.DynamoGSIKey;
import com.chainsync.dynamodb.annotation.DynamoKey;
import com.chainsync.dynamodb.annotation.DynamoTable;
import com.chainsync.dynamodb.model.PaginationDTO;
import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
import com.chainsync.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;
import com.chainsync.dynamodb.util.DynamoDBLocalProvider;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
public class BaseQueryDaoTest {

  private static final String TABLE_NAME = "test-table";
  private static final String PK = "pk";
  private static final String RK = "rk";
  private static final String ATTR1 = "attr1";
  private static final String ATTR2 = "attr2";
  private static final String ATTR3 = "attr3";
  private static final String ATTR4 = "attr4";
  private static final String ATTR5 = "attr5";
  private static final String ATTR6 = "attr6";
  private static final Map<String, Long> ATTR3_MAP = ImmutableMap.of("a", 1L);
  private static final List<Integer> ATTR4_LIST = List.of(1, 2, 3);
  private static final TestObject TEST_OBJ1 =
      new TestObject("1", 1, "1", TestEnum.VAL1, ATTR3_MAP, ATTR4_LIST, "1", "1");
  private static final TestObject TEST_OBJ12 =
      new TestObject("1", 2, "1", TestEnum.VAL2, ATTR3_MAP, ATTR4_LIST, "1", "1");
  private static final TestObject TEST_OBJ2 =
      new TestObject("2", 2, "2", TestEnum.VAL1, ATTR3_MAP, ATTR4_LIST, "2", "2");
  private static final TestObject TEST_OBJ3 =
      new TestObject("3", 3, "3", TestEnum.VAL2, ATTR3_MAP, ATTR4_LIST, "3", "3");
  private static TestQueryDao dao;

  @BeforeAll
  public static void setup() {
    final DynamoDB dynamoDB = new DynamoDB(DynamoDBLocalProvider.getInstance());
    final DynamoDBTableOrmManager<TestObject> ormManager =
        new AnnotatedDynamoDBTableOrmManager<>("test-table", TestObject.class);
    dao = new TestQueryDao(ormManager, dynamoDB);

    dao.putItem(TEST_OBJ1);
    dao.putItem(TEST_OBJ12);
    dao.putItem(TEST_OBJ2);
    dao.putItem(TEST_OBJ3);
  }

  @AfterAll
  public static void tearDown() {
    DynamoDBLocalProvider.tearDown();
  }

  @Test
  void testDaoCreation() {
    Assertions.assertNotNull(dao);
  }

  @Test
  void testGetItem() {
    final TestObject testObject1 = dao.getItem("1", 1);
    final TestObject testObject2 = dao.getItem("2", 2);
    Assertions.assertEquals(TEST_OBJ1, testObject1);
    Assertions.assertEquals(TEST_OBJ2, testObject2);
  }

  @Test
  void testPutItem() {
    final TestObject testObject1 = dao.putItem(TEST_OBJ1);
    Assertions.assertEquals(TEST_OBJ1, testObject1);
    Assertions.assertThrows(
        ConditionalCheckFailedException.class, () -> dao.putItemDeduped(TEST_OBJ1));
  }

  @Test
  void testUpdateItem() {
    TestObject updatedItem =
        dao.updateItem("1", 1, List.of(new AttributeUpdate(ATTR1).put("2")), null);
    Assertions.assertEquals("2", updatedItem.getAttr1());
    updatedItem = dao.updateItem("1", 1, List.of(new AttributeUpdate(ATTR1).put("1")), null);
    Assertions.assertEquals("1", updatedItem.getAttr1());
  }

  @Test
  void testQueryItemByPartitionKey() {
    final List<TestObject> res1 = dao.queryByPartitionKey("1");
    final List<TestObject> res2 = dao.queryByPartitionKey("2");
    Assertions.assertEquals(2, res1.size());
    Assertions.assertEquals(1, res2.size());
    Assertions.assertEquals(TEST_OBJ1, res1.get(0));
    Assertions.assertEquals(TEST_OBJ2, res2.get(0));
  }

  @Test
  void testQueryPaginateByPartitionKey() {
    final PaginationDTO<TestObject> res1 = dao.queryPaginateByPartitionKey("1", null, 1);
    final PaginationDTO<TestObject> res2 = dao.queryPaginateByPartitionKey("1", null, 10);
    Assertions.assertEquals(1, res1.getTotalCount());
    Assertions.assertEquals("1", res1.getCursor().get(PK).getS());
    Assertions.assertEquals(TEST_OBJ1, res1.getItems().get(0));
    Assertions.assertEquals(2, res2.getTotalCount());
    Assertions.assertNull(res2.getCursor());
  }

  @Test
  void testQueryItemByRangeKey() {
    final List<TestObject> queryRes = dao.queryByRange("1", null, 2);
    Assertions.assertEquals(2, queryRes.size());
  }

  @Test
  void testQueryByRange() {
    List<TestObject> queryRes = dao.queryByRange("1", 1, 2);
    Assertions.assertEquals(2, queryRes.size());
    queryRes = dao.queryByRange("1", null, 2);
    Assertions.assertEquals(2, queryRes.size());
    queryRes = dao.queryByRange("1", 1, null);
    Assertions.assertEquals(2, queryRes.size());
  }

  @Test
  void testScan() {
    final List<TestObject> queryRes = dao.scan();
    Assertions.assertEquals(4, queryRes.size());
  }

  @Test
  void testScanWithFilter() {
    final List<TestObject> queryRes = dao.scanWithFilter(new ScanFilter("attr1").contains("1"));
    Assertions.assertEquals(2, queryRes.size());
  }

  @Test
  void scanWithFilterPagination() {
    final PaginationDTO queryRes =
        dao.scanWithFilterPagination(0, 1, new ScanFilter("attr1").contains("1"));
    Assertions.assertEquals(2, queryRes.getTotalCount());
  }

  @Test
  void testScanByRange() {
    final List<TestObject> queryRes = dao.scanByRange(1, 3);
    Assertions.assertEquals(4, queryRes.size());
  }

  @Test
  @Order(Integer.MAX_VALUE)
  // execute it last
  void testWriteBatch() {
    final List<TestObject> batch = new ArrayList<>();
    for (int i = 0; i < 50; i++) {
      batch.add(TEST_OBJ3.toBuilder().rk(i + 10).build());
    }
    dao.batchPutItem(batch);
    final List<TestObject> objects = dao.scan();
    Assertions.assertEquals(54, objects.size());
  }

  @Test
  void testBatchGet() {
    final List<TestObject> testObjects =
        dao.batchGetItems(List.of(new PrimaryKey(PK, "1", RK, 1), new PrimaryKey(PK, "1", RK, 2)));
    Assertions.assertEquals(2, testObjects.size());
  }

  @Test
  void testQuerySortAndRangeKeyOnGsi() {
    final List<TestObject> queryRes =
        dao.queryByPartitionKeyAndSortKeyOnGsi(
            "attr5", "1", new RangeKeyCondition("attr5").eq("1"));
    Assertions.assertEquals(2, queryRes.size());
    Assertions.assertTrue(queryRes.contains(TEST_OBJ1));
    Assertions.assertTrue(queryRes.contains(TEST_OBJ12));
    final List<TestObject> queryRes2 =
        dao.queryByPartitionKeyAndSortKeyOnGsi(
            "attr5", "2", new RangeKeyCondition("attr5").eq("2"));
    Assertions.assertEquals(1, queryRes2.size());
    Assertions.assertEquals(TEST_OBJ2, queryRes2.get(0));
  }

  // TODO implement query by range on GSI later
  @Test
  @Disabled
  void testQueryByRangeOnGsi() {
    final List<TestObject> queryRes = dao.queryByRangeOnGsi("attr1", "1", "1", "2");
    Assertions.assertEquals(2, queryRes.size());
    Assertions.assertEquals(TEST_OBJ1, queryRes.get(0));
    Assertions.assertEquals(TEST_OBJ12, queryRes.get(1));
  }

  public enum TestEnum {
    VAL1,
    VAL2
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder(toBuilder = true)
  @DynamoTable(globalSecondaryIndices = {"attr1", "attr5", "attr6"})
  public static class TestObject {

    @DynamoKey(dynamoKeyType = DynamoKey.HASH)
    @DynamoGSIKey(dynamoGSINames = {"attr5", "attr6"})
    @DynamoAttribute(attributeName = PK, attributeType = "S")
    private String pk;

    @DynamoKey(dynamoKeyType = DynamoKey.RANGE)
    @DynamoAttribute(attributeName = RK, attributeType = "N")
    private Integer rk;

    @DynamoKey(dynamoKeyType = DynamoKey.HASH, dynamoGSIName = "attr1")
    @DynamoAttribute(attributeName = ATTR1, attributeType = "S")
    private String attr1;

    @DynamoAttribute(attributeName = ATTR2, attributeType = "S")
    private TestEnum attr2;

    @DynamoAttribute(attributeName = ATTR3)
    private Map<String, Long> attr3;

    @DynamoAttribute(attributeName = ATTR4)
    private List<Integer> attr4;

    @DynamoGSIKey(dynamoGSINames = "attr5", dynamoKeyType = DynamoGSIKey.RANGE)
    @DynamoAttribute(attributeName = ATTR5)
    private String attr5;

    @DynamoGSIKey(dynamoGSINames = "attr6", dynamoKeyType = DynamoGSIKey.RANGE)
    @DynamoAttribute(attributeName = ATTR6)
    private String attr6;
  }

  static class TestQueryDao extends BaseQueryDao<TestObject> {

    public TestQueryDao(
        @NonNull final DynamoDBTableOrmManager<TestObject> ormManager,
        @NonNull final DynamoDB dynamoDB) {
      super(ormManager, dynamoDB);
    }
  }
}
