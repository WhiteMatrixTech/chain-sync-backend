package com.matrix.blockchain.dao;

import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.blockchain.constants.Constants;
import com.matrix.blockchain.model.BlockRange;
import com.matrix.blockchain.model.BlockTip;
import com.matrix.dynamodb.dao.BaseQueryDao;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * @author luyuanheng
 */
@Log4j2
@Component
public class BlockTipDao extends BaseQueryDao<BlockTip> {

  public BlockTipDao(final DynamoDBTableOrmManager<BlockTip> ormManager, final DynamoDB dynamoDB) {
    super(ormManager, dynamoDB);
  }

  public void updateTip(final BlockRange blockRange) {
    BlockTip blockTip = this.getItem(blockRange.getChainId());
    if (blockTip == null) {
      this.putItem(new BlockTip(blockRange.getChainId(), Constants.NULL_BLOCK));
    } else {
      if (isNextFrom(blockTip.getBlockNumber(), blockRange.getFrom())) {
        List<AttributeUpdate> list = new ArrayList<>();
        list.add(new AttributeUpdate(BlockTip.ATTR_BLOCK_NUMBER).put(blockRange.getTo()));
        this.updateItem(blockRange.getChainId(), list, null);
      }
    }
  }

  private boolean isNextFrom(Long latestTo, Long from) {
    return from != null && latestTo != null && from - latestTo == 1L;
  }
}
