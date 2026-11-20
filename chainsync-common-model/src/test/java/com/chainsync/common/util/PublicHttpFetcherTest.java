package com.chainsync.common.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PublicHttpFetcherTest {

  private static final String METADATA = "{\"name\":\"token #1\"}";

  private HttpServer server;
  private String base;
  private final AtomicInteger secretRequests = new AtomicInteger();

  /** For the tests 127.0.0.1 plays the public host, every other address is refused. */
  private final PublicHttpFetcher fetcher =
      new PublicHttpFetcher(address -> "127.0.0.1".equals(address.getHostAddress()));

  @BeforeEach
  void startServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    final int port = server.getAddress().getPort();
    base = "http://127.0.0.1:" + port;
    server.createContext("/metadata.json", exchange -> respond(exchange, 200, METADATA));
    server.createContext("/a b.json", exchange -> respond(exchange, 200, METADATA));
    server.createContext("/missing", exchange -> respond(exchange, 404, "not found"));
    server.createContext("/relative", exchange -> redirect(exchange, "/metadata.json"));
    server.createContext("/loop", exchange -> redirect(exchange, "/loop"));
    server.createContext("/to-file", exchange -> redirect(exchange, "file:///etc/passwd"));
    server.createContext(
        "/to-other-address",
        exchange -> redirect(exchange, "http://127.0.0.2:" + port + "/secret"));
    server.createContext(
        "/to-ipv6-loopback", exchange -> redirect(exchange, "http://[::1]:" + port + "/secret"));
    server.createContext(
        "/secret",
        exchange -> {
          secretRequests.incrementAndGet();
          respond(exchange, 200, "secret");
        });
    server.start();
  }

  @AfterEach
  void stopServer() {
    server.stop(0);
  }

  @Test
  void privateAndSpecialAddressesAreNotPublic() throws IOException {
    final String[] addresses = {
      "0.0.0.0",
      "0.1.2.3",
      "10.0.0.1",
      "100.64.0.1",
      "100.100.100.200",
      "127.0.0.1",
      "127.8.8.8",
      "169.254.169.254",
      "172.16.0.1",
      "172.31.255.255",
      "192.0.0.8",
      "192.168.1.1",
      "198.18.0.1",
      "224.0.0.1",
      "240.0.0.1",
      "255.255.255.255",
      "::",
      "::1",
      "::ffff:127.0.0.1",
      "::ffff:10.0.0.1",
      "::10.0.0.1",
      "64:ff9b::a9fe:a9fe",
      "fe80::1",
      "fec0::1",
      "fc00::1",
      "fd00:ec2::254",
      "ff02::1"
    };
    for (final String address : addresses) {
      Assertions.assertFalse(
          PublicHttpFetcher.isPublicAddress(InetAddress.getByName(address)), address);
    }
  }

  @Test
  void otherAddressesArePublic() throws IOException {
    final String[] addresses = {
      "1.1.1.1",
      "8.8.8.8",
      "100.63.255.255",
      "100.128.0.1",
      "169.253.0.1",
      "172.15.0.1",
      "172.32.0.1",
      "192.0.1.1",
      "192.167.0.1",
      "198.17.0.1",
      "198.20.0.1",
      "223.255.255.255",
      "::ffff:8.8.8.8",
      "64:ff9b::808:808",
      "2001:4860:4860::8888"
    };
    for (final String address : addresses) {
      Assertions.assertTrue(
          PublicHttpFetcher.isPublicAddress(InetAddress.getByName(address)), address);
    }
  }

  @Test
  void isPublicHttpUrl() {
    Assertions.assertTrue(PublicHttpFetcher.isPublicHttpUrl("https://8.8.8.8/metadata/1.json"));
    Assertions.assertTrue(PublicHttpFetcher.isPublicHttpUrl("HTTP://8.8.8.8:8080/1"));
    Assertions.assertTrue(PublicHttpFetcher.isPublicHttpUrl("http://[2001:4860:4860::8888]/1"));
    Assertions.assertTrue(PublicHttpFetcher.isPublicHttpUrl("https://8.8.8.8/token 1.json"));

    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl("http://127.0.0.1:8080/actuator/env"));
    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl("http://localhost/"));
    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl("http://[::1]/"));
    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl("http://[::ffff:127.0.0.1]/"));
    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl("http://0.0.0.0/"));
    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl("http://10.1.2.3/"));
    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl("http://192.168.0.10:9200/_cat"));
    Assertions.assertFalse(
        PublicHttpFetcher.isPublicHttpUrl("http://169.254.169.254/latest/meta-data/"));
    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl("http://user@127.0.0.1/"));
    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl("http://8.8.8.8@127.0.0.1/"));

    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl("ftp://8.8.8.8/1.json"));
    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl("file:///etc/passwd"));
    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl("ipfs://Qm123/1.json"));
    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl("data:application/json,{}"));
    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl("/metadata/1.json"));
    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl("http:///metadata/1.json"));
    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl("not a url"));
    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl(""));
    Assertions.assertFalse(PublicHttpFetcher.isPublicHttpUrl(null));
  }

  @Test
  void fetchReturnsBody() throws IOException {
    Assertions.assertEquals(METADATA, fetcher.fetch(base + "/metadata.json"));
  }

  @Test
  void fetchEncodesUrlsThatAreNotEncoded() throws IOException {
    Assertions.assertEquals(METADATA, fetcher.fetch(base + "/a b.json"));
  }

  @Test
  void fetchFollowsRedirectToAllowedAddress() throws IOException {
    Assertions.assertEquals(METADATA, fetcher.fetch(base + "/relative"));
  }

  @Test
  void fetchRefusesAddressThatIsNotAllowed() {
    final String url = base.replace("127.0.0.1", "127.0.0.2") + "/secret";
    Assertions.assertThrows(IOException.class, () -> fetcher.fetch(url));
    final PublicHttpFetcher defaultFetcher = new PublicHttpFetcher();
    Assertions.assertThrows(IOException.class, () -> defaultFetcher.fetch(base + "/secret"));
    Assertions.assertEquals(0, secretRequests.get());
  }

  @Test
  void fetchRefusesRedirectToAddressThatIsNotAllowed() {
    Assertions.assertThrows(IOException.class, () -> fetcher.fetch(base + "/to-other-address"));
    Assertions.assertThrows(IOException.class, () -> fetcher.fetch(base + "/to-ipv6-loopback"));
    Assertions.assertEquals(0, secretRequests.get());
  }

  @Test
  void fetchRefusesRedirectToOtherScheme() {
    Assertions.assertThrows(IOException.class, () -> fetcher.fetch(base + "/to-file"));
  }

  @Test
  void fetchStopsAfterTooManyRedirects() {
    Assertions.assertThrows(IOException.class, () -> fetcher.fetch(base + "/loop"));
  }

  @Test
  void fetchFailsOnErrorStatus() {
    Assertions.assertThrows(IOException.class, () -> fetcher.fetch(base + "/missing"));
  }

  private static void redirect(final HttpExchange exchange, final String location)
      throws IOException {
    exchange.getResponseHeaders().add("Location", location);
    exchange.sendResponseHeaders(302, -1);
    exchange.close();
  }

  private static void respond(final HttpExchange exchange, final int status, final String body)
      throws IOException {
    final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.sendResponseHeaders(status, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.close();
  }
}
