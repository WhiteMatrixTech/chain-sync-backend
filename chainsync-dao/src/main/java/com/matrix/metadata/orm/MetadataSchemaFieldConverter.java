package com.matrix.metadata.orm;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.matrix.dynamodb.orm.FieldConverter;
import com.matrix.metadata.model.NftCollection;
import com.matrix.metadata.model.NftMetadataField;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;

/**
 * @author yangjian
 * @date 2022/1/11 PM 2:09
 */
public class MetadataSchemaFieldConverter implements FieldConverter {

  @Override
  @SneakyThrows
  public Item convertFieldAndAddToDBItem(final Object object, final Item item) {
    final List<NftMetadataField> metadataSchema = ((NftCollection) object).getMetadataSchema();
    if (metadataSchema == null) {
      return item;
    }
    final List<Map<String, ?>> schemaMap =
        metadataSchema.stream()
            .map(NftMetadataField::toMapRepresentation)
            .collect(Collectors.toList());
    item.withList(NftCollection.ATTR_METADATA_SCHEMA, schemaMap);
    return item;
  }

  @Override
  @SneakyThrows
  public Object convertDBFieldAndAddToObject(final Object object, final Item item) {
    final NftCollection nftCollection = (NftCollection) object;
    final List<Map<String, ?>> schemaMap = item.getList(NftCollection.ATTR_METADATA_SCHEMA);
    if (schemaMap == null) {
      nftCollection.setMetadataSchema(null);
    } else {
      final List<NftMetadataField> metadataSchema =
          schemaMap.stream()
              .map(map -> new NftMetadataField().fromMapRepresentation(map))
              .collect(Collectors.toList());
      nftCollection.setMetadataSchema(metadataSchema);
    }
    return nftCollection;
  }
}
