package com.chainsync.etl.service;

import com.chainsync.etl.model.Token;
import java.util.List;

/**
 * @author reimia
 */
public interface TokenService {

  List<Token> scanEthereumToken();

  List<Token> queryEthereumToken(String address, String tokenId);

  List<Token> queryEthereumTokenByOwner(String owner);

}
