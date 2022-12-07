package com.matrix.eventhandler.module;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.matrix.blockchain.dao.ContractTemplateDao;
import com.matrix.dynamodb.orm.DynamoDBTableOrmManager;
import com.matrix.dynamodb.orm.impl.AnnotatedDynamoDBTableOrmManager;
import com.matrix.eventhandler.model.Token;
import com.matrix.metadata.dao.NftCollectionDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author reimia
 */
@Configuration
public class DynamoDbModule {

  @Value("${spring.profiles.active}")
  private String env;

  @Value("${dynamodb.nft-collection-name}")
  private String collectionTableName;

  @Value("${dynamodb.blockchain-contract-template-table-name}")
  private String templateTableName;

  @Value("${dynamodb.token-table-name}")
  private String tokenTableName;

  @Bean
  public NftCollectionDao nftCollectionDao(final DynamoDB dynamoDB) {
    return new NftCollectionDao(collectionTableName, dynamoDB, env);
  }

  @Bean
  public ContractTemplateDao contractTemplateDao(final DynamoDB dynamoDB) {
    return new ContractTemplateDao(templateTableName, dynamoDB);
  }

  @Bean("tokenOrmManager")
  public DynamoDBTableOrmManager<Token> openSeaIdentifierOrmManager() {
    return new AnnotatedDynamoDBTableOrmManager<>(
        tokenTableName, Token.class);
  }

}
