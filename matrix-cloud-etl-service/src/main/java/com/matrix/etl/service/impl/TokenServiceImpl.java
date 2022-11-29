package com.matrix.etl.service.impl;

import com.matrix.etl.dao.TokenDao;
import com.matrix.etl.model.Token;
import com.matrix.etl.service.TokenService;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * @author reimia
 */
@Log4j2
@Service
public class TokenServiceImpl implements TokenService {

  @Resource TokenDao tokenDao;

  @Override
  public List<Token> scanEthereumToken() {
    return tokenDao.scanWithLimit(50);
  }

  @Override
  public List<Token> queryEthereumToken(final String address, final String tokenId) {
    if (tokenId != null) {
      final Token item = tokenDao.getItem(address, tokenId);
      if (item == null) {
        return List.of();
      }
      return List.of(item);
    }
    return tokenDao.queryByPartitionKey(address);
  }

  @Override
  public List<Token> queryEthereumTokenByOwner(final String owner) {
    return tokenDao.queryByPartitionKeyOnGsi(Token.INDEX_OWNER, owner);
  }

}
