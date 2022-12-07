package com.chainsync.dynamodb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface DynamoKey {

  public static final String HASH = "hash";
  public static final String RANGE = "range";

  String dynamoKeyType() default HASH;

  String readCapacity() default "5";

  String writeCapacity() default "5";

  String dynamoGSIName() default "";
}
