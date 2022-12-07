package com.matrix.dynamodb.orm;

import com.google.common.collect.ImmutableMap;
import com.matrix.common.model.Address;
import com.matrix.common.model.ChainId;
import com.matrix.common.model.TokenId;
import com.matrix.common.model.TransactionHash;
import com.matrix.dynamodb.orm.impl.BigDecimalDynamoDBItemAggregator;
import com.matrix.dynamodb.orm.impl.BigIntegerDynamoDBItemAggregator;
import com.matrix.dynamodb.orm.impl.BooleanDynamoDBItemAggregator;
import com.matrix.dynamodb.orm.impl.InstantDynamoDBItemAggregator;
import com.matrix.dynamodb.orm.impl.IntegerDynamoDBItemAggregator;
import com.matrix.dynamodb.orm.impl.ListDynamoDBItemAggregator;
import com.matrix.dynamodb.orm.impl.LongDynamoDBItemAggregator;
import com.matrix.dynamodb.orm.impl.MapDynamoDBItemAggregator;
import com.matrix.dynamodb.orm.impl.StringDynamoDBItemAggregator;
import com.matrix.dynamodb.orm.impl.ToStringDynamoDBItemAggregator;
import com.matrix.dynamodb.orm.impl.TokenIdDynamoDBItemAggregator;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class DynamoDBItemAggregators {

  private static final Map<Class<?>, DynamoDBItemAggregator> AGGREGATORS =
      ImmutableMap.<Class<?>, DynamoDBItemAggregator>builder()
          .put(String.class, new StringDynamoDBItemAggregator())
          .put(Integer.class, new IntegerDynamoDBItemAggregator())
          .put(Long.class, new LongDynamoDBItemAggregator())
          .put(BigDecimal.class, new BigDecimalDynamoDBItemAggregator())
          .put(BigInteger.class, new BigIntegerDynamoDBItemAggregator())
          .put(Instant.class, new InstantDynamoDBItemAggregator())
          .put(Enum.class, new ToStringDynamoDBItemAggregator())
          .put(Map.class, new MapDynamoDBItemAggregator())
          .put(List.class, new ListDynamoDBItemAggregator())
          .put(Address.class, new ToStringDynamoDBItemAggregator())
          .put(ChainId.class, new ToStringDynamoDBItemAggregator())
          .put(TransactionHash.class, new ToStringDynamoDBItemAggregator())
          .put(Boolean.class, new BooleanDynamoDBItemAggregator())
          .put(TokenId.class, new TokenIdDynamoDBItemAggregator())
          .build();

  private DynamoDBItemAggregators() {
    throw new IllegalStateException("Utility class");
  }

  public static DynamoDBItemAggregator getAggregator(final Class<?> javaType) {
    return AGGREGATORS.get(javaType);
  }
}
