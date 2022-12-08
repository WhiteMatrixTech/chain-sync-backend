package com.chainsync.etl.service;

import com.chainsync.common.model.Address;
import com.chainsync.etl.model.Token;
import java.util.List;

/**
 * @author reimia
 */
public interface TokenService {

  List<Token> scanEthereumToken();

  List<Token> queryEthereumToken(String address, String tokenId);

  List<Token> queryEthereumTokenByOwner(String owner);

  List<Token> scanFlowToken();

  List<Token> queryFlowToken(String address, String tokenId);

  List<Token> queryFlowTokenByOwner(String owner);

  void refreshEthereumTokenMetadata(final Address address, final String tokenId);

}
