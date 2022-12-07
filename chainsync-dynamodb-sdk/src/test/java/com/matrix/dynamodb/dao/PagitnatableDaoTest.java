package com.matrix.dynamodb.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.google.common.collect.ImmutableMap;
import com.matrix.dynamodb.annotation.DynamoAttribute;
import com.matrix.dynamodb.annotation.DynamoKey;
import com.matrix.dynamodb.annotation.DynamoTable;
import com.matrix.dynamodb.model.PaginatedQueryResult;
import com.matrix.dynamodb.model.PaginatedQuerySpec;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
import com.matrix.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;
import com.matrix.dynamodb.util.DynamoDBLocalProvider;
import java.util.Comparator;
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
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
public class PagitnatableDaoTest {

  private static final String TABLE_NAME = "test-table";
  private static final String PK = "pk";
  private static final String RK = "rk";
  private static final String ATTR1 = "attr1";
  private static final String ATTR2 = "attr2";
  private static final String ATTR3 = "attr3";
  private static final String ATTR4 = "attr4";
  private static final Map<String, Long> ATTR3_MAP = ImmutableMap.of("a", 1L);
  private static final List<Integer> ATTR4_LIST = List.of(1, 2, 3);
  private static final TestObject TEST_OBJ1 =
      new TestObject("1", 1, "1", TestEnum.VAL1, ATTR3_MAP, ATTR4_LIST);
  private static final TestObject TEST_OBJ2 =
      new TestObject("1", 2, "1", TestEnum.VAL2, ATTR3_MAP, ATTR4_LIST);
  private static final TestObject TEST_OBJ3 =
      new TestObject("1", 3, "2", TestEnum.VAL1, ATTR3_MAP, ATTR4_LIST);
  private static final TestObject TEST_OBJ4 =
      new TestObject("1", 4, "3", TestEnum.VAL2, ATTR3_MAP, ATTR4_LIST);
  private static final TestObject TEST_OBJ5 =
      new TestObject("1", 5, "3", TestEnum.VAL2, ATTR3_MAP, ATTR4_LIST);
  private static TestQueryDao dao;

  @BeforeAll
  public static void setup() {
    final DynamoDB dynamoDB = new DynamoDB(DynamoDBLocalProvider.getInstance());
    final DynamoDBTableOrmManager<TestObject> ormManager =
        new AnnotatedDynamoDBTableOrmManager<>("test-table", TestObject.class);
    dao = new TestQueryDao(ormManager, dynamoDB);

    dao.putItem(TEST_OBJ1);
    dao.putItem(TEST_OBJ2);
    dao.putItem(TEST_OBJ3);
    dao.putItem(TEST_OBJ4);
    dao.putItem(TEST_OBJ5);
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
  @Order(1)
  void testRangeQuery() {
    final int from = 1;
    final int limit = 2;
    final int to = 5;
    final PaginatedQueryResult<TestObject, Integer> page1 =
        dao.queryPaginatedResult(
            "1", PaginatedQuerySpec.<Integer>builder().from(from).to(to).limit(limit).build());
    System.out.println("page1: " + page1.toString());
    final PaginatedQuerySpec spec2 =
        PaginatedQuerySpec.<Integer>builder()
            .from(from)
            .to(to)
            .limit(limit)
            .paginator(page1.getPaginator())
            .build();
    System.out.println("spec2: " + spec2.toString());
    final PaginatedQueryResult<TestObject, Integer> page2 = dao.queryPaginatedResult("1", spec2);
    final PaginatedQuerySpec spec3 =
        PaginatedQuerySpec.<Integer>builder()
            .from(from)
            .to(to)
            .limit(limit)
            .paginator(page2.getPaginator())
            .build();
    System.out.println("spec3: " + spec3.toString());
    final PaginatedQueryResult<TestObject, Integer> page3 = dao.queryPaginatedResult("1", spec3);
    Assertions.assertEquals(2, page1.getItems().size());
    Assertions.assertEquals(2, page2.getItems().size());
    Assertions.assertEquals(1, page3.getItems().size());
    Assertions.assertNull(page3.getPaginator());
  }

  @Test
  @Order(2)
  void testRangeQueryWithoutTo() {
    final int from = 1;
    final int limit = 2;
    final PaginatedQueryResult<TestObject, Integer> page1 =
        dao.queryPaginatedResult(
            "1", PaginatedQuerySpec.<Integer>builder().from(from).limit(limit).build());
    System.out.println("page1: " + page1.toString());
    final PaginatedQuerySpec spec2 =
        PaginatedQuerySpec.<Integer>builder()
            .from(page1.getPaginator())
            .paginator(page1.getPaginator())
            .limit(limit)
            .build();
    System.out.println("spec2: " + spec2.toString());
    final PaginatedQueryResult<TestObject, Integer> page2 = dao.queryPaginatedResult("1", spec2);
    final PaginatedQuerySpec spec3 =
        PaginatedQuerySpec.<Integer>builder()
            .from(page2.getPaginator())
            .paginator(page2.getPaginator())
            .limit(limit)
            .build();
    System.out.println("spec3: " + spec3.toString());
    final PaginatedQueryResult<TestObject, Integer> page3 = dao.queryPaginatedResult("1", spec3);
    Assertions.assertEquals(2, page1.getItems().size());
    Assertions.assertEquals(2, page2.getItems().size());
    Assertions.assertEquals(1, page3.getItems().size());
    Assertions.assertNull(page3.getPaginator());
  }

  @Test
  @Order(3)
  void testRangeQueryWithoutFrom() {
    final int limit = 2;
    final int to = 5;
    final PaginatedQueryResult<TestObject, Integer> page1 =
        dao.queryPaginatedResult(
            "1", PaginatedQuerySpec.<Integer>builder().to(to).limit(limit).build());
    System.out.println("page1: " + page1.toString());
    final PaginatedQuerySpec spec2 =
        PaginatedQuerySpec.<Integer>builder()
            .paginator(page1.getPaginator())
            .to(to)
            .limit(limit)
            .build();
    System.out.println("spec2: " + spec2.toString());
    final PaginatedQueryResult<TestObject, Integer> page2 = dao.queryPaginatedResult("1", spec2);
    final PaginatedQuerySpec spec3 =
        PaginatedQuerySpec.<Integer>builder()
            .paginator(page2.getPaginator())
            .to(to)
            .limit(limit)
            .build();
    System.out.println("spec3: " + spec3.toString());
    final PaginatedQueryResult<TestObject, Integer> page3 = dao.queryPaginatedResult("1", spec3);
    Assertions.assertEquals(2, page1.getItems().size());
    Assertions.assertEquals(2, page2.getItems().size());
    Assertions.assertEquals(1, page3.getItems().size());
    Assertions.assertNull(page3.getPaginator());
  }

  @Test
  @Order(4)
  void testRangeQueryWithoutParams() {
    final PaginatedQueryResult<TestObject, Integer> page1 =
        dao.queryPaginatedResult("1", PaginatedQuerySpec.<Integer>builder().build());
    System.out.println("page1: " + page1.toString());
    Assertions.assertEquals(5, page1.getItems().size());
    Assertions.assertNull(page1.getPaginator());
  }

  public enum TestEnum {
    VAL1,
    VAL2
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder(toBuilder = true)
  @DynamoTable(globalSecondaryIndices = {"attr1"})
  public static class TestObject {

    @DynamoKey(dynamoKeyType = DynamoKey.HASH)
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
  }

  static class TestQueryDao extends PaginatableDao<TestObject, Integer> {

    public TestQueryDao(
        @NonNull final DynamoDBTableOrmManager<TestObject> ormManager,
        @NonNull final DynamoDB dynamoDB) {
      super(obj -> obj.getRk(), Comparator.comparingInt(Integer::intValue), ormManager, dynamoDB);
    }
  }
}
