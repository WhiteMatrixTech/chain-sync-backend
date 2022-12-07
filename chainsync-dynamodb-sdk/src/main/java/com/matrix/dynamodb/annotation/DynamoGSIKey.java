package com.matrix.dynamodb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * if one DynamoAttribute is bind to multiple KeyDefinition (GSi), use this annotation
 *
 * @author reimia
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface DynamoGSIKey {

  String HASH = "hash";
  String RANGE = "range";

  String dynamoKeyType() default HASH;

  String[] dynamoGSINames();

  String readCapacity() default "5";

  String writeCapacity() default "5";

  /** only work when dynamoKeyType() is HASH */
  boolean isKeysOnly() default false;
}
