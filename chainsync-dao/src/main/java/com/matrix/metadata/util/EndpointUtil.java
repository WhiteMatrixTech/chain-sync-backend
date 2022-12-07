package com.matrix.metadata.util;

import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author shuyizhang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EndpointUtil {

  private static final String METADATA_URL_TEMPLATE =
      "/metadata/api/v1/apps/%s/contracts/%s/metadata/tokens/";
  private static final Map<String, String> ONE_SYNC_BASE_URL =
      Map.of(
          "local",
          "http://localhost:8080",
          "beta",
          "https://api.1sync-staging.nft1.global",
          "alpha",
          "https://dev68bac.1sync.services",
          "staging",
          "https://98kszqpv.1sync.services",
          "prod",
          "https://api.1sync.services");

  public static String build1SyncMetadataEndpoint(
      final String env, final String appId, final String contractId) {
    final String baseUrl = ONE_SYNC_BASE_URL.get(env);
    if (baseUrl == null) {
      throw new IllegalArgumentException("Invalid env: " + env);
    }
    return baseUrl + String.format(METADATA_URL_TEMPLATE, appId, contractId);
  }
}
