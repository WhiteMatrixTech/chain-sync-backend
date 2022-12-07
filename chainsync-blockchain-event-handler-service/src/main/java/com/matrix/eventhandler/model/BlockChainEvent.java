package com.matrix.eventhandler.model;

import com.matrix.common.model.Address;
import java.util.List;

/**
 * @author reimia
 */
public interface BlockChainEvent {
  Address getContract();

  String getEventName();

  List<String> getEventTags();
}
