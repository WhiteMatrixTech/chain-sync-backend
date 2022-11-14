package com.matrix.dynamodb.model;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yangjian
 * @date 2021/12/31 PM 2:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginationDTO<OrmType> {

  List<OrmType> items;

  Integer totalCount;

  Map<String, AttributeValue> cursor;
}
