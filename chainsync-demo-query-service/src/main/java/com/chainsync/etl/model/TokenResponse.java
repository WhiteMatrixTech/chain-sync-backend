package com.chainsync.etl.model;

import com.chainsync.etl.util.GsonUtil;
import java.util.Map;
import lombok.Data;

/**
 * @author reimia
 */
@Data
public class TokenResponse {

  public TokenResponse(final Token token) {
    this.address = token.getAddress();
    this.tokenId = token.getTokenId();
    this.owner = token.getOwner();
    this.tokenMetadataURI = token.getTokenMetadataURI();
    final Map map;
    final Metadata metadata1 = new Metadata();
    try {
      map = GsonUtil.GSON.fromJson(token.getTokenMetadataRaw(), Map.class);
      metadata1.setName((String) map.get("name"));
      metadata1.setImage((String) map.get("image"));
      metadata1.setDescription((String) map.get("description"));
      if (map.get("attributes") != null) {
        metadata1.setAttributes(GsonUtil.GSON.toJson(map.get("attributes")));
      }
    } catch (final Exception e) {
      this.metadata = metadata1;
      return;
    }
    this.metadata = metadata1;
  }

  private final String address;
  private final String tokenId;
  private final String owner;
  private final String tokenMetadataURI;
  private final Metadata metadata;

  @Data
  public static class Metadata {
    private String name;
    private String image;
    private String description;
    private String attributes;
  }
}
