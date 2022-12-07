package com.chainsync.eventhandler.model;

import com.chainsync.common.model.Address;
import java.util.List;

/**
 * @author reimia
 */
public interface BlockChainEvent {
  Address getContract();

  String getEventName();

  List<String> getEventTags();
}
