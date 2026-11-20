package com.chainsync.common.util;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Predicate;

/**
 * Fetches documents from URLs we do not control, for example the tokenURI returned by a contract.
 *
 * <p>Such a URL can point to an address that is only reachable from inside our own network
 * (loopback, private ranges, the cloud metadata service). To prevent server side request forgery,
 * only http(s) URLs are requested and only if every address the host resolves to is a public one.
 * Redirects are followed by hand so that every hop goes through the same check.
 *
 * <p>The host is resolved once for the check and once more by the HTTP client. Both lookups are
 * answered from the JVM address cache, so do not disable it (networkaddress.cache.ttl=0) in a
 * service that uses this class.
 */
public class PublicHttpFetcher {

  private static final int MAX_REDIRECTS = 5;
  private static final Duration TIMEOUT = Duration.ofSeconds(10);

  private final Predicate<InetAddress> allowedAddress;
  private final HttpClient httpClient;

  public PublicHttpFetcher() {
    this(PublicHttpFetcher::isPublicAddress);
  }

  /** Only for tests, which have to talk to a server on the loopback interface. */
  PublicHttpFetcher(final Predicate<InetAddress> allowedAddress) {
    this.allowedAddress = allowedAddress;
    this.httpClient =
        HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(TIMEOUT)
            .build();
  }

  /**
   * @param url the url to check
   * @return true if url is a http(s) url and its host only resolves to public addresses
   */
  public static boolean isPublicHttpUrl(final String url) {
    try {
      checkHost(toHttpUri(url), PublicHttpFetcher::isPublicAddress);
      return true;
    } catch (final IOException e) {
      return false;
    }
  }

  /**
   * GET the document behind url.
   *
   * @param url a http(s) url
   * @return the response body
   * @throws IOException if the url or one of its redirect targets is not a public http(s) url, or
   *     if the request fails
   */
  public String fetch(final String url) throws IOException {
    URI uri = toHttpUri(url);
    for (int i = 0; i <= MAX_REDIRECTS; i++) {
      checkHost(uri, allowedAddress);
      final HttpRequest request = HttpRequest.newBuilder(uri).timeout(TIMEOUT).GET().build();
      final HttpResponse<String> response;
      try {
        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IOException("interrupted while requesting " + uri, e);
      }
      final int status = response.statusCode();
      if (status >= 300 && status < 400) {
        final String location = response.headers().firstValue("Location").orElse(null);
        if (location == null) {
          throw new IOException("redirect without location from " + uri);
        }
        uri = toHttpUri(uri.resolve(toUri(location)).toString());
        continue;
      }
      if (status < 200 || status >= 300) {
        throw new IOException("unexpected status " + status + " from " + uri);
      }
      return response.body();
    }
    throw new IOException("too many redirects: " + url);
  }

  /**
   * @param address a resolved address
   * @return false for loopback, private, link local and other special purpose addresses
   */
  static boolean isPublicAddress(final InetAddress address) {
    final byte[] bytes = address.getAddress();
    if (bytes.length == 4) {
      return isPublicIpv4(bytes[0] & 0xff, bytes[1] & 0xff, bytes[2] & 0xff);
    }
    if (address.isAnyLocalAddress()
        || address.isLoopbackAddress()
        || address.isLinkLocalAddress()
        || address.isSiteLocalAddress()
        || address.isMulticastAddress()) {
      return false;
    }
    // unique local addresses, fc00::/7
    if ((bytes[0] & 0xfe) == 0xfc) {
      return false;
    }
    // an IPv4 address inside an IPv6 one: ::a.b.c.d and NAT64, 64:ff9b::a.b.c.d
    if (((Inet6Address) address).isIPv4CompatibleAddress() || isNat64(bytes)) {
      return isPublicIpv4(bytes[12] & 0xff, bytes[13] & 0xff, bytes[14] & 0xff);
    }
    return true;
  }

  private static boolean isPublicIpv4(final int a, final int b, final int c) {
    return !(a == 0 // 0.0.0.0/8, this network
        || a == 10 // 10.0.0.0/8, private
        || (a == 100 && (b & 0xc0) == 64) // 100.64.0.0/10, shared address space
        || a == 127 // 127.0.0.0/8, loopback
        || (a == 169 && b == 254) // 169.254.0.0/16, link local, cloud metadata service
        || (a == 172 && (b & 0xf0) == 16) // 172.16.0.0/12, private
        || (a == 192 && b == 0 && c == 0) // 192.0.0.0/24, protocol assignments
        || (a == 192 && b == 168) // 192.168.0.0/16, private
        || (a == 198 && (b & 0xfe) == 18) // 198.18.0.0/15, benchmarking
        || a >= 224); // multicast, reserved, broadcast
  }

  private static boolean isNat64(final byte[] bytes) {
    final int[] prefix = {0x00, 0x64, 0xff, 0x9b, 0, 0, 0, 0, 0, 0, 0, 0};
    for (int i = 0; i < prefix.length; i++) {
      if ((bytes[i] & 0xff) != prefix[i]) {
        return false;
      }
    }
    return true;
  }

  private static void checkHost(final URI uri, final Predicate<InetAddress> allowedAddress)
      throws IOException {
    for (final InetAddress address : InetAddress.getAllByName(uri.getHost())) {
      if (!allowedAddress.test(address)) {
        throw new IOException("refusing to request " + uri + ", " + address + " is not public");
      }
    }
  }

  private static URI toHttpUri(final String url) throws IOException {
    final URI uri = toUri(url);
    final String scheme = uri.getScheme();
    if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
      throw new IOException("not a http(s) url: " + url);
    }
    if (uri.getHost() == null) {
      throw new IOException("no host in url: " + url);
    }
    return uri;
  }

  private static URI toUri(final String url) throws IOException {
    if (url == null) {
      throw new IOException("url is null");
    }
    try {
      return new URI(url.trim());
    } catch (final URISyntaxException e) {
      // urls found in the wild are not always encoded, for example they contain spaces
      try {
        final URL parsed = new URL(url.trim());
        return new URI(
            parsed.getProtocol(),
            parsed.getUserInfo(),
            parsed.getHost(),
            parsed.getPort(),
            parsed.getPath(),
            parsed.getQuery(),
            parsed.getRef());
      } catch (final MalformedURLException | URISyntaxException ignored) {
        throw new IOException("invalid url: " + url, e);
      }
    }
  }
}
