package com.matrix.eventhandler.abi;

import com.matrix.blockchain.dao.ContractTemplateDao;
import com.matrix.blockchain.model.ContractTemplate;
import com.matrix.common.model.Address;
import com.matrix.eventhandler.abi.reader.AbiReader;
import com.matrix.eventhandler.model.AbiEnhancedEvent;
import com.matrix.metadata.dao.NftCollectionDao;
import com.matrix.metadata.model.NftCollection;
import java.util.ArrayList;
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

  private static final String TRANSFER_EVENT_STRING = "[\n"
      + "  {\n"
      + "    \"anonymous\": false,\n"
      + "    \"inputs\": [\n"
      + "      {\n"
      + "        \"indexed\": true,\n"
      + "        \"internalType\": \"address\",\n"
      + "        \"name\": \"from\",\n"
      + "        \"type\": \"address\"\n"
      + "      },\n"
      + "      {\n"
      + "        \"indexed\": true,\n"
      + "        \"internalType\": \"address\",\n"
      + "        \"name\": \"to\",\n"
      + "        \"type\": \"address\"\n"
      + "      },\n"
      + "      {\n"
      + "        \"indexed\": true,\n"
      + "        \"internalType\": \"uint256\",\n"
      + "        \"name\": \"tokenId\",\n"
      + "        \"type\": \"uint256\"\n"
      + "      }\n"
      + "    ],\n"
      + "    \"name\": \"Transfer\",\n"
      + "    \"type\": \"event\"\n"
      + "  }\n"
      + "]";

  private static final AbiEnhancedEvent baseTransferEvent = AbiReader.readEvents(
      TRANSFER_EVENT_STRING, null).get(0);

  private final NftCollectionDao nftCollectionDao;
  private final ContractTemplateDao contractTemplateDao;

  private final Map<Address, List<AbiEnhancedEvent>> map = new HashMap<>();

  /**
   * default resolve all below event
   * <p>
   * - event Transfer(address indexed _from, address indexed _to, uint256 indexed _tokenId);
   * <p>
   * get contractAddress from nftCollection table and read its abi from contractTemplate table
   */
  public List<AbiEnhancedEvent> getAbiEvent(final Address contractAddress) {
    if (map.containsKey(contractAddress)) {
      return map.get(contractAddress);
    }
    List<AbiEnhancedEvent> events = new ArrayList<>();
    events.add(baseTransferEvent);
    final NftCollection nftCollection = nftCollectionDao.getNftCollection(contractAddress);
    if (nftCollection == null) {
      return events;
    }
    final String templateId = nftCollection.getTemplateId();
    if (templateId == null) {
      return events;
    }
    final ContractTemplate contractTemplate = contractTemplateDao.getItem(templateId);
    final String abi = contractTemplate.getAbi();
    final List<AbiEnhancedEvent> abiEnhancedEvents = AbiReader.readEvents(abi);
    events.addAll(abiEnhancedEvents);
    map.put(contractAddress, events);
    return events;
  }
}
