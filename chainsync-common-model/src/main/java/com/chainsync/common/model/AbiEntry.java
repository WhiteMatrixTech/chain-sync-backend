package com.chainsync.common.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author shuyizhang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = Id.NAME, property = "type", defaultImpl = AbiEntryType.class, visible = true,
        include = As.EXISTING_PROPERTY)
@JsonSubTypes({@JsonSubTypes.Type(value = AbiEvent.class, names = {"event", "EVENT"}),
        @JsonSubTypes.Type(value = AbiFunction.class, names = {"function", "FUNCTION"}),
        @JsonSubTypes.Type(value = AbiConstructor.class, names = {"constructor", "CONSTRUCTOR"}),})
@SuperBuilder
public class AbiEntry {
    private Boolean anonymous;
    private String name;
    private List<AbiParam> inputs;
    private AbiEntryType type;
}
