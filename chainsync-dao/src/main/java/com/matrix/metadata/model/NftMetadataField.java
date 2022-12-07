package com.matrix.metadata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.matrix.common.model.MapRepresentable;
import com.matrix.dynamodb.util.SerdeUtil;
import com.matrix.metadata.util.StrictStringDeserializer;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @author yangjian
 * @date 2022/1/10 PM 4:10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NftMetadataField implements MapRepresentable<NftMetadataField> {

  @JsonDeserialize(using = StrictStringDeserializer.class)
  @NonNull
  private String fieldName;

  @NonNull private FieldType fieldType;

  @JsonProperty("isIndex")
  private boolean isIndex;

  @JsonProperty("isTrait")
  private boolean isTrait;

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, ?> toMapRepresentation() {
    return SerdeUtil.DEFAULT_OBJECT_MAPPER.convertValue(this, Map.class);
  }

  @Override
  public NftMetadataField fromMapRepresentation(@NonNull final Map<String, ?> mapRepresentation) {
    final NftMetadataField field =
        SerdeUtil.DEFAULT_OBJECT_MAPPER.convertValue(mapRepresentation, NftMetadataField.class);
    setFieldName(field.getFieldName());
    setFieldType(field.getFieldType());
    setIndex(field.isIndex());
    setTrait(field.isTrait());
    return this;
  }
}
