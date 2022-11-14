package com.matrix.blockchain.model;

import lombok.Getter;

/**
 * @author: ZhangKai
 */
@Getter
public enum BlockchainType {
  RINKEBY(
      "rinkeby_ethereum",
      "rinkebyWeb3j",
      "rinkebyBlockEventDao",
      null,
      "rinkebyBlockchainLogKafkaClient",
      null),

  ETHEREUM(
      "mainnet_ethereum",
      "ethereumWeb3j",
      "ethereumBlockEventDao",
      null,
      "ethereumBlockchainLogKafkaClient",
      null),

  MUMBAI(
      "mumbai_polygon",
      "mumbaiWeb3j",
      "mumbaiBlockEventDao",
      null,
      "mumbaiBlockchainLogKafkaClient",
      null),

  POLYGON(
      "mainnet_polygon",
      "polygonWeb3j",
      "polygonBlockEventDao",
      null,
      "polygonBlockchainLogKafkaClient",
      null),

  FLOW_TEST_NET(
      "testnet_flow",
      null,
      "flowTestNetBlockEventDao",
      "flowTestNetBlockchainTransactionDao",
      "flowTestNetBlockchainLogKafkaClient",
      "flowTestNetTransactionKafkaClient"),

  FLOW_MAIN_NET(
      "mainnet_flow",
      null,
      "flowMainNetBlockEventDao",
      "flowMainNetBlockchainTransactionDao",
      "flowMainNetBlockchainLogKafkaClient",
      "flowMainNetTransactionKafkaClient",
      "flowMainNetTransactionHistoryKafkaClient");
  private String chainId;

  private String web3jName;

  private String blockEventDaoName;

  private String transactionDaoName;

  private String blockchainLogKafkaClientName;

  private String blockchainTransactionKafkaClientName;

  private String blockchainTransactionHistoryKafkaClientName;

  BlockchainType(
      final String chainId,
      final String web3jName,
      final String blockEventDaoName,
      final String transactionDaoName,
      final String blockchainLogKafkaClientName,
      final String blockchainTransactionKafkaClientName) {
    this.chainId = chainId;
    this.web3jName = web3jName;
    this.blockEventDaoName = blockEventDaoName;
    this.transactionDaoName = transactionDaoName;
    this.blockchainLogKafkaClientName = blockchainLogKafkaClientName;
    this.blockchainTransactionKafkaClientName = blockchainTransactionKafkaClientName;
  }

  BlockchainType(
      final String chainId,
      final String web3jName,
      final String blockEventDaoName,
      final String transactionDaoName,
      final String blockchainLogKafkaClientName,
      final String blockchainTransactionKafkaClientName,
      final String blockchainTransactionHistoryKafkaClientName) {
    this.chainId = chainId;
    this.web3jName = web3jName;
    this.blockEventDaoName = blockEventDaoName;
    this.transactionDaoName = transactionDaoName;
    this.blockchainLogKafkaClientName = blockchainLogKafkaClientName;
    this.blockchainTransactionKafkaClientName = blockchainTransactionKafkaClientName;
    this.blockchainTransactionHistoryKafkaClientName = blockchainTransactionHistoryKafkaClientName;
  }

  public static String getWeb3j(String chainId) {
    for (BlockchainType e : values()) {
      if (chainId.equals(e.chainId)) {
        return e.web3jName;
      }
    }
    throw new UnsupportedOperationException(
        String.format("Current web3j is not supported, chainId is [%s]", chainId));
  }

  public static String getBlockEventDao(String chainId) {
    for (BlockchainType e : values()) {
      if (chainId.equals(e.chainId)) {
        return e.blockEventDaoName;
      }
    }
    throw new UnsupportedOperationException(
        String.format("Current blockEventDao is not supported, chainId is [%s]", chainId));
  }

  public static String getBlockchainTransactionDao(String chainId) {
    for (BlockchainType e : values()) {
      if (chainId.equals(e.chainId)) {
        return e.transactionDaoName;
      }
    }
    throw new UnsupportedOperationException(
        String.format("Current transactionDao is not supported, chainId is [%s]", chainId));
  }

  public static String getBlockchainLogKafkaClient(String chainId) {
    for (BlockchainType e : values()) {
      if (chainId.equals(e.chainId)) {
        return e.blockchainLogKafkaClientName;
      }
    }
    throw new UnsupportedOperationException(
        String.format(
            "Current blockchainLogKafkaClient is not supported, chainId is [%s]", chainId));
  }

  public static String getBlockchainTransactionKafkaClient(String chainId) {
    for (BlockchainType e : values()) {
      if (chainId.equals(e.chainId)) {
        return e.blockchainTransactionKafkaClientName;
      }
    }
    throw new UnsupportedOperationException(
        String.format(
            "Current blockchainTransactionKafkaClient is not supported, chainId is [%s]", chainId));
  }

  public static String getBlockchainTransactionHistoryKafkaClient(String chainId) {
    for (BlockchainType e : values()) {
      if (chainId.equals(e.chainId)) {
        return e.blockchainTransactionHistoryKafkaClientName;
      }
    }
    throw new UnsupportedOperationException(
        String.format(
            "Current blockchainTransactionHistoryKafkaClient is not supported, chainId is [%s]",
            chainId));
  }
}
