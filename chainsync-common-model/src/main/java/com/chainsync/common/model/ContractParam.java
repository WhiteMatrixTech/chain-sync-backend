package com.chainsync.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractParam {
  private String type;
  private Object value;

  public Type convertToWeb3AbiType() {
    switch (this.getType()) {
      case "string":
        return new Utf8String((String) this.getValue());
      case "string[]":
        return new DynamicArray<>(Utf8String.class, ((List<?>) this.getValue()).stream()
            .map(item -> new Utf8String(item.toString())).collect(Collectors.toList()));
      case "uint256":
        return new Uint256((BigInteger) this.getValue());
      case "uint256[]":
        return new DynamicArray<>(Uint256.class, ((List<?>) this.getValue()).stream()
            .map(item -> new Uint256((BigInteger) item)).collect(Collectors.toList()));
      case "address":
        return new Address((String) this.getValue());
      case "address[]":
        return new DynamicArray<>(Address.class, ((List<?>) this.getValue()).stream()
            .map(item -> new Address(item.toString())).collect(Collectors.toList()));
      case "bool":
        return new Bool((boolean) this.getValue());
      default:
        throw new IllegalArgumentException("Unsupported type: " + this.getType());
    }
  }
}
