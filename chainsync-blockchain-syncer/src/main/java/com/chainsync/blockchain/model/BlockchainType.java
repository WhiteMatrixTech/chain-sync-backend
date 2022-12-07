package com.chainsync.blockchain.model;

import lombok.Getter;

/**
 * @author: ZhangKai
 */
@Getter
public enum BlockchainType {
  ETHEREUM(
      "mainnet_ethereum",
      "ethereumWeb3j",
      "ethereumBlockEventDao",
      "ethereumTransactionDao",
      "ethereumBlockchainLogKafkaClient",
      null),

  BSC(
      "mainnet_bsc",
      "bscWeb3j",
      "bscBlockEventDao",
      "bscTransactionDao",
      "bscBlockchainLogKafkaClient",
      null),

  POLYGON(
      "mainnet_polygon",
      "polygonWeb3j",
      "polygonBlockEventDao",
      "polygonTransactionDao",
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
  private final String chainId;

  private final String web3jName;

  private final String blockEventDaoName;

  private final String transactionDaoName;

  private final String blockchainLogKafkaClientName;

  private final String blockchainTransactionKafkaClientName;

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

  public static String getWeb3j(final String chainId) {
    for (final BlockchainType e : values()) {
      if (chainId.equals(e.chainId)) {
        return e.web3jName;
      }
    }
    throw new UnsupportedOperationException(
        String.format("Current web3j is not supported, chainId is [%s]", chainId));
  }

  public static String getBlockEventDao(final String chainId) {
    for (final BlockchainType e : values()) {
      if (chainId.equals(e.chainId)) {
        return e.blockEventDaoName;
      }
    }
    throw new UnsupportedOperationException(
        String.format("Current blockEventDao is not supported, chainId is [%s]", chainId));
  }

  public static String getBlockchainTransactionDao(final String chainId) {
    for (final BlockchainType e : values()) {
      if (chainId.equals(e.chainId)) {
        return e.transactionDaoName;
      }
    }
    throw new UnsupportedOperationException(
        String.format("Current transactionDao is not supported, chainId is [%s]", chainId));
  }

  public static String getBlockchainLogKafkaClient(final String chainId) {
    for (final BlockchainType e : values()) {
      if (chainId.equals(e.chainId)) {
        return e.blockchainLogKafkaClientName;
      }
    }
    throw new UnsupportedOperationException(
        String.format(
            "Current blockchainLogKafkaClient is not supported, chainId is [%s]", chainId));
  }

  public static String getBlockchainTransactionKafkaClient(final String chainId) {
    for (final BlockchainType e : values()) {
      if (chainId.equals(e.chainId)) {
        return e.blockchainTransactionKafkaClientName;
      }
    }
    throw new UnsupportedOperationException(
        String.format(
            "Current blockchainTransactionKafkaClient is not supported, chainId is [%s]", chainId));
  }

  public static String getBlockchainTransactionHistoryKafkaClient(final String chainId) {
    for (final BlockchainType e : values()) {
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
