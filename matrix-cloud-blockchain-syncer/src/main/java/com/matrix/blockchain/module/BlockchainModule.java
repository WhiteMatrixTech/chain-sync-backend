package com.matrix.blockchain.module;

import com.matrix.blockchain.config.BlockchainConfig;
import com.matrix.blockchain.model.Web3jContainer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Resource;
import org.apache.commons.compress.utils.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

/**
 * @author shuyizhang
 */
@Configuration
public class BlockchainModule {

  @Resource private BlockchainConfig blockchainConfig;

  /**
   * rinkeby
   *
   * @return
   */
  @Bean("rinkebyWeb3j")
  public Web3jContainer getRinkebyWeb3j() {
    List<Web3j> web3jList = Lists.newArrayList();
    blockchainConfig.getRinkebyProviderEndpoint().stream()
        .forEach(endpoint -> web3jList.add(Web3j.build(new HttpService(endpoint))));
    return Web3jContainer.builder().web3jList(web3jList).index(new AtomicInteger()).build();
  }

  /**
   * ethereum
   *
   * @return
   */
  @Bean("ethereumWeb3j")
  public Web3jContainer getEthereumWeb3j() {
    List<Web3j> web3jList = Lists.newArrayList();
    blockchainConfig.getEthereumProviderEndpoint().stream()
        .forEach(endpoint -> web3jList.add(Web3j.build(new HttpService(endpoint))));
    return Web3jContainer.builder().web3jList(web3jList).index(new AtomicInteger()).build();
  }

  /**
   * mumbai
   *
   * @return
   */
  @Bean("mumbaiWeb3j")
  public Web3jContainer getMumbaiWeb3j() {
    List<Web3j> web3jList = Lists.newArrayList();
    blockchainConfig.getMumbaiProviderEndpoint().stream()
        .forEach(endpoint -> web3jList.add(Web3j.build(new HttpService(endpoint))));
    return Web3jContainer.builder().web3jList(web3jList).index(new AtomicInteger()).build();
  }

  /**
   * polygon
   *
   * @return
   */
  @Bean("polygonWeb3j")
  public Web3jContainer getPolygonWeb3j() {
    List<Web3j> web3jList = Lists.newArrayList();
    blockchainConfig.getPolygonProviderEndpoint().stream()
        .forEach(endpoint -> web3jList.add(Web3j.build(new HttpService(endpoint))));
    return Web3jContainer.builder().web3jList(web3jList).index(new AtomicInteger()).build();
  }
}
