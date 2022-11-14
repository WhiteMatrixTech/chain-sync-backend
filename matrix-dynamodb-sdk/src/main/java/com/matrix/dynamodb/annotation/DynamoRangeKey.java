package com.matrix.dynamodb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * if one DynamoAttribute is bind to multiple KeyDefinition (GSi), use this annotation
 * combine @DynamoHashKey
 *
 * @author luyuanheng
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface DynamoRangeKey {

  String[] dynamoGSINames();
}
