package com.chainsync.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.chainsync.blockchain.model.BlockOffset;
import com.chainsync.blockchain.model.BlockRange;
import com.chainsync.dynamodb.dao.BaseQueryDao;
import com.chainsync.dynamodb.orm.DynamoDBTableOrmManager;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * @author luyuanheng
 */
@Log4j2
@Component
public class BlockOffsetDao extends BaseQueryDao<BlockOffset> {

  public BlockOffsetDao(
      final DynamoDBTableOrmManager<BlockOffset> ormManager, final DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }

  public void updateOffset(final BlockRange blockRange) {
    BlockOffset blockOffset = this.getItem(blockRange.getChainId(), blockRange.getStart());
    if (blockOffset == null) {
      blockOffset =
          this.putItem(
              BlockOffset.builder()
                  .chainId(blockRange.getChainId())
                  .start(blockRange.getStart())
                  .end(blockRange.getEnd())
                  .offset(blockRange.getStart())
                  .build());
    }

    if (isNextFrom(blockOffset.getOffset(), blockRange.getFrom())) {
      List<AttributeUpdate> list = new ArrayList<>();
      list.add(new AttributeUpdate(BlockOffset.ATTR_OFFSET).put(blockRange.getTo()));
      this.updateItem(blockRange.getChainId(), blockRange.getStart(), list, null);
    }
  }

  private boolean isNextFrom(Long latestTo, Long from) {
    return from != null && latestTo != null && Math.abs(from - latestTo) == 1L;
  }
}
