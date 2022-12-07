package com.matrix.common.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.AbiTypes;
import org.web3j.abi.datatypes.Type;

/**
 * @author shuyizhang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbiParam {

  public AbiParam(
      final String name, final Boolean indexed, final String internalType, final String type) {
    this.name = name;
    this.indexed = indexed;
    this.internalType = internalType;
    this.type = type;
  }

  private String name;
  private Boolean indexed;
  private String internalType;
  private String type;
  private List<AbiParam> components;

  public TypeReference<? extends Type> toWeb3AbiType() {
    return TypeReference.create(AbiTypes.getType(this.type), this.getIndexed());
  }
}
