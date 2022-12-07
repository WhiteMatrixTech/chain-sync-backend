package com.chainsync.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * @author yangjian
 */
@Value
@Builder
@Jacksonized
public class Erc721MetadataTrait {

  @JsonProperty("trait_type")
  String traitName;

  Object value;
}
