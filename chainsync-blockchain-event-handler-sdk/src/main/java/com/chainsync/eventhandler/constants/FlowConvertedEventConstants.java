package com.chainsync.eventhandler.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author reimia
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlowConvertedEventConstants {

  public static final String BURN_EVENT = "burn";
  public static final String MINT_EVENT = "mint";
  public static final String TRANSFER_EVENT = "transfer";
  public static final String SALE_EVENT = "sale";
  public static final String LISTING_EVENT = "listing";
  public static final String LISTING_COMPLETED_EVENT = "listingCompleted";
  public static final String OFFER_EVENT = "offer";
  public static final String OFFER_COMPLETED_EVENT = "offerCompleted";
}
