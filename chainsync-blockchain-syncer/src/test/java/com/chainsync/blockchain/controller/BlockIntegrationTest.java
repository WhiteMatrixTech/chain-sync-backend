package com.chainsync.blockchain.controller;

import com.chainsync.blockchain.processor.FlowEventProcessor;
import com.chainsync.common.model.ChainName;
import com.chainsync.common.model.ChainType;
import java.util.List;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import schedule.proto.Block;

@SpringBootTest
class BlockIntegrationTest {
  private static final List<Long> BLOCKS = List.of(34180961L, 34078188L);
  @Resource FlowEventProcessor processor;

  @Value("${s3.event.flow.bucket}")
  private String flowEventBucket;

  @SneakyThrows
  @Test
  void test() {
    List<Block> blocksFromS3 =
        processor.getBlocksFromS3(
            ChainType.flow.name(), ChainName.mainnet.name(), flowEventBucket, BLOCKS);
    for (Block block : blocksFromS3) {
      System.out.println(block);
    }
  }
}
