package com.matrix.dynamodb.util;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import java.net.ServerSocket;
import lombok.SneakyThrows;

/** @author kilru */
public class DynamoDBLocalProvider {

  private static AmazonDynamoDB dynamodbClient = null;
  private static DynamoDBProxyServer dynamoProxy = null;
  private static int port;

  private DynamoDBLocalProvider() {
    throw new IllegalStateException("Utility class");
  }

  @SneakyThrows
  private static int getFreePort() {
    ServerSocket socket = new ServerSocket(0);
    int port = socket.getLocalPort();
    socket.close();
    return port;
  }

  @SneakyThrows
  public static AmazonDynamoDB getInstance() {
    if (dynamodbClient == null) {
      setup();
      dynamodbClient =
          AmazonDynamoDBClientBuilder.standard()
              .withEndpointConfiguration(
                  new AwsClientBuilder.EndpointConfiguration(
                      "http://localhost:" + port, Regions.US_EAST_1.getName()))
              .withCredentials(
                  new AWSStaticCredentialsProvider(
                      // DynamoDB Local works with any non-null credentials
                      new BasicAWSCredentials("", "")))
              .build();
    }
    return dynamodbClient;
  }

  @SneakyThrows
  private static void setup() {
    System.setProperty(
        "sqlite4java.library.path", "../matrix-dynamodb-sdk/src/main/resources/libs/");
    if (dynamoProxy == null) {
      port = getFreePort();
      dynamoProxy =
          ServerRunner.createServerFromCommandLineArgs(
              new String[] {"-inMemory", "-port", Integer.toString(port)});
      dynamoProxy.start();
    }
  }

  @SneakyThrows
  public static void tearDown() {
    dynamoProxy.stop();
    dynamoProxy = null;
    dynamodbClient = null;
  }
}
