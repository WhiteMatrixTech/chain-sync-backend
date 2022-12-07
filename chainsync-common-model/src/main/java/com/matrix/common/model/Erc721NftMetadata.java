package com.matrix.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * @author shuyizhang
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Erc721NftMetadata {
  @NonNull String name;

  @NonNull String description;

  @NonNull String image;

  String externalUrl;

  @JsonProperty("animation_url")
  String animationUrl;

  @JsonProperty("background_color")
  String backgroundColor;

  @JsonProperty("youtube_url")
  String youtubeUrl;

  @NonNull Set<Erc721MetadataTrait> attributes;
}
