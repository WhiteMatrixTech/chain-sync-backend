package com.matrix.blockchain.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Builder;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.util.CollectionUtils;
import org.web3j.protocol.Web3j;

/**
 * @author luyuanheng
 */
@Builder
public class Web3jContainer {

  private AtomicInteger index;
  private List<Web3j> web3jList;

  public Web3j getWeb3j() {
    if (CollectionUtils.isEmpty(web3jList)) {
      throw new BeanInitializationException("web3j config is empty");
    }

    if (index.get() >= web3jList.size()) {
      index = new AtomicInteger(0);
    }
    return web3jList.get(index.getAndIncrement());
  }

  public int getWeb3jSize() {
    return web3jList.size();
  }
}
