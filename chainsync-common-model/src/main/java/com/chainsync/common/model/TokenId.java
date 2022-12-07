package com.chainsync.common.model;

import com.google.protobuf.ByteString;
import java.math.BigInteger;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * NFT tokenId. A BigInteger value.
 *
 * @author ShenYang
 */
@Data
@AllArgsConstructor
public class TokenId {

  /**
   * token id value
   */
  @Nonnull
  private BigInteger val;

  /**
   * Convert to gRPC ByteString.java type. Data type in protobuf is 'bytes'
   *
   * @return ByteString
   */
  public ByteString toProtobuf() {
    return ByteString.copyFrom(val.toByteArray());
  }

  /**
   * Parse from a gRPC ByteString
   *
   * @param val ByteString value
   * @return token id
   */
  public static TokenId fromProtobuf(final ByteString val) {
    return new TokenId(new BigInteger(val.toByteArray()));
  }

  /**
   * Parse from a string number
   *
   * @param val string value
   * @return token id
   */
  public static TokenId from(final String val) {
    return new TokenId(new BigInteger(val));
  }

  /**
   * Parse from an int number
   *
   * @param val int value
   * @return token id
   */
  public static TokenId from(final int val) {
    return new TokenId(BigInteger.valueOf(val));
  }

  /**
   * BigInteger value to string
   *
   * @return BigInteger string
   */
  @Override
  public String toString() {
    return val.toString();
  }

  /**
   * To a padding length 16 hex string
   *
   * @return hex string
   */
  public String toUint64HexString() {
    int length = 16;
    String hex = val.toString(16);
    int padding = length - hex.length();
    if (padding < 0) {
      throw new IllegalStateException("value greater than uint64");
    }
    return "0".repeat(padding).concat(hex);
  }

  /**
   * Create a TokenId from a hex string
   *
   * @param hex hex string
   * @return TokenId
   */
  public static TokenId fromHexString(String hex) {
    return new TokenId(new BigInteger(hex, 16));
  }
}
