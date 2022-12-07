package com.matrix.blockchain.orm;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matrix.blockchain.model.BlockchainTransaction;
import com.matrix.blockchain.model.BlockchainTransaction.Receipt;
import com.matrix.blockchain.model.EthereumReceipt;
import com.matrix.common.model.ChainType;
import com.matrix.dynamodb.orm.FieldConverter;
import lombok.SneakyThrows;

/**
 * transaction receipt field converter
 *
 * @author ShenYang
 */
public class TransactionReceiptFieldConverter implements FieldConverter {

  /**
   * json mapper
   */
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  @SneakyThrows
  public Item convertFieldAndAddToDBItem(Object object, Item item) {
    if (!(object instanceof BlockchainTransaction)) {
      return item;
    }

    Receipt receipt = ((BlockchainTransaction) object).getReceipt();
    if (receipt == null) {
      return item;
    }

    return item.withString(BlockchainTransaction.ATTR_RECEIPT,
        objectMapper.writeValueAsString(receipt)
    );
  }

  @Override
  @SneakyThrows
  public Object convertDBFieldAndAddToObject(Object object, Item item) {
    String receiptStr = item.getString(BlockchainTransaction.ATTR_RECEIPT);
    if (receiptStr == null || receiptStr.isBlank()) {
      return object;
    }

    BlockchainTransaction transaction = (BlockchainTransaction) object;
    ChainType chainType = transaction.getChainId().getChainType();
    if (chainType == ChainType.ethereum || chainType == ChainType.polygon) {
      transaction.setReceipt(objectMapper.readValue(receiptStr, EthereumReceipt.class));
      return transaction;
    }

    throw new UnsupportedOperationException(
        String.format("Current chain[%s] is not supported!", chainType));
  }

  /**
   * receipt to json string
   *
   * @param receipt receipt
   * @return receipt json string
   */
  @SneakyThrows
  public String receiptToString(Receipt receipt) {
    return receipt == null ? null : objectMapper.writeValueAsString(receipt);
  }
}
