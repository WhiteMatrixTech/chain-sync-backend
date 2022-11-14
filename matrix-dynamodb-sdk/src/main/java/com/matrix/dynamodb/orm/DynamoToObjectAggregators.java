package com.matrix.dynamodb.orm;

import com.google.common.collect.ImmutableMap;
import com.matrix.common.model.Address;
import com.matrix.common.model.ChainId;
import com.matrix.common.model.TokenId;
import com.matrix.common.model.TransactionHash;
import com.matrix.dynamodb.orm.impl.AddressDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.BigDecimalDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.BigDecimalListDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.BigDecimalMapDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.BigIntegerDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.BooleanDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.ChainIdDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.EnumDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.InstantDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.IntegerDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.IntegerListDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.IntegerMapDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.LongDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.LongListDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.LongMapDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.StringDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.StringListDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.StringMapDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.TokenIdDynamoToObjectAggregator;
import com.matrix.dynamodb.orm.impl.TransactionHashDynamoToObjectAggregator;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class DynamoToObjectAggregators {

  private static final Map<Class<?>, DynamoToObjectAggregator> AGGREGATORS =
      ImmutableMap.<Class<?>, DynamoToObjectAggregator>builder()
          .put(String.class, new StringDynamoToObjectAggregator())
          .put(Integer.class, new IntegerDynamoToObjectAggregator())
          .put(Long.class, new LongDynamoToObjectAggregator())
          .put(BigDecimal.class, new BigDecimalDynamoToObjectAggregator())
          .put(BigInteger.class, new BigIntegerDynamoToObjectAggregator())
          .put(Instant.class, new InstantDynamoToObjectAggregator())
          .put(Enum.class, new EnumDynamoToObjectAggregator())
          .put(Address.class, new AddressDynamoToObjectAggregator())
          .put(ChainId.class, new ChainIdDynamoToObjectAggregator())
          .put(TransactionHash.class, new TransactionHashDynamoToObjectAggregator())
          .put(Boolean.class, new BooleanDynamoToObjectAggregator())
          .put(TokenId.class, new TokenIdDynamoToObjectAggregator())
          .build();

  private static final Map<Class<?>, DynamoToObjectAggregator> MAP_AGGREGATORS =
      ImmutableMap.<Class<?>, DynamoToObjectAggregator>builder()
          .put(String.class, new StringMapDynamoToObjectAggregator())
          .put(Integer.class, new IntegerMapDynamoToObjectAggregator())
          .put(Long.class, new LongMapDynamoToObjectAggregator())
          .put(BigDecimal.class, new BigDecimalMapDynamoToObjectAggregator())
          .build();

  private static final Map<Class<?>, DynamoToObjectAggregator> LIST_AGGREGATORS =
      ImmutableMap.<Class<?>, DynamoToObjectAggregator>builder()
          .put(String.class, new StringListDynamoToObjectAggregator())
          .put(Integer.class, new IntegerListDynamoToObjectAggregator())
          .put(Long.class, new LongListDynamoToObjectAggregator())
          .put(BigDecimal.class, new BigDecimalListDynamoToObjectAggregator())
          .build();

  private DynamoToObjectAggregators() {
    throw new IllegalStateException("Utility class");
  }

  public static DynamoToObjectAggregator getAggregator(
      final Class<?> javaType, final Class<?> genericType) {
    if (genericType != null) {
      if (javaType.equals(Map.class)) {
        return MAP_AGGREGATORS.get(genericType);
      } else if (javaType.equals(List.class)) {
        return LIST_AGGREGATORS.get(genericType);
      } else {
        return null;
      }
    }
    return AGGREGATORS.get(javaType);
  }
}
