package com.chainsync.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author reimia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemId implements StringRepresentable<ItemId> {

  private static final String SEPARATOR = "#";

  private Address address;
  private TokenId tokenId;

  @Override
  public String toStringRepresentation() {
    return address.toString() + SEPARATOR + tokenId.getVal();
  }

  @Override
  public ItemId fromStringRepresentation(final String stringRepresentation) {
    final String[] split = stringRepresentation.split(SEPARATOR);
    setAddress(Address.fromFullAddress(split[0]));
    setTokenId(TokenId.from(split[1]));
    return this;
  }

  @Override
  public String toString() {
    return toStringRepresentation();
  }
}
