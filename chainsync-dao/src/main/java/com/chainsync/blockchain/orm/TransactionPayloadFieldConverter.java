package com.chainsync.blockchain.orm;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.chainsync.blockchain.model.BlockchainTransaction;
import com.chainsync.blockchain.model.BlockchainTransaction.Payload;
import com.chainsync.blockchain.model.EthereumPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.chainsync.common.model.ChainType;
import com.chainsync.dynamodb.orm.FieldConverter;
import lombok.SneakyThrows;

/**
 * transaction payload field converter
 *
 * @author ShenYang
 */
public class TransactionPayloadFieldConverter implements FieldConverter {

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

    Payload payload = ((BlockchainTransaction) object).getPayload();
    if (payload == null) {
      return item;
    }

    return item.withString(BlockchainTransaction.ATTR_PAYLOAD,
        objectMapper.writeValueAsString(payload)
    );
  }

  @Override
  @SneakyThrows
  public Object convertDBFieldAndAddToObject(Object object, Item item) {
    String payloadStr = item.getString(BlockchainTransaction.ATTR_PAYLOAD);
    if (payloadStr == null || payloadStr.isBlank()) {
      return object;
    }

    BlockchainTransaction transaction = (BlockchainTransaction) object;
    ChainType chainType = transaction.getChainId().getChainType();
    if (chainType == ChainType.ethereum || chainType == ChainType.polygon) {
      transaction.setPayload(objectMapper.readValue(payloadStr, EthereumPayload.class));
      return transaction;
    }

    throw new UnsupportedOperationException(
        String.format("Current chain[%s] is not supported!", chainType));
  }
}
