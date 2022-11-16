package com.matrix.eventhandler.abi;

import com.matrix.blockchain.dao.ContractTemplateDao;
import com.matrix.blockchain.model.ContractTemplate;
import com.matrix.common.model.Address;
import com.matrix.eventhandler.abi.reader.AbiReader;
import com.matrix.eventhandler.model.AbiEnhancedEvent;
import com.matrix.metadata.dao.NftCollectionDao;
import com.matrix.metadata.model.NftCollection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * manage contract address map to its abi
 *
 * @author reimia
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AbiEnhancedEventManager {

  private final NftCollectionDao nftCollectionDao;
  private final ContractTemplateDao contractTemplateDao;

  // TODO cache instead
  private final Map<Address, List<AbiEnhancedEvent>> map = new HashMap<>();

  /** get contractAddress from nftCollection table and read its abi from contractTemplate table */
  public List<AbiEnhancedEvent> getAbiEvent(final Address contractAddress) {
    if (map.containsKey(contractAddress)) {
      return map.get(contractAddress);
    }
    final NftCollection nftCollection = nftCollectionDao.getNftCollection(contractAddress);
    if (nftCollection == null) {
      return Collections.emptyList();
    }
    final String templateId = nftCollection.getTemplateId();
    if (templateId == null) {
      return Collections.emptyList();
    }
    final ContractTemplate contractTemplate = contractTemplateDao.getItem(templateId);
    final String abi = contractTemplate.getAbi();
    final List<AbiEnhancedEvent> abiEnhancedEvents = AbiReader.readEvents(abi);
    map.put(contractAddress, abiEnhancedEvents);
    return abiEnhancedEvents;
  }
}
