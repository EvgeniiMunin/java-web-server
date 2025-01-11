package org.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
@JsonDeserialize(builder = ExtImpPrebid.ExtImpPrebidBuilder.class)
public class ExtImpPrebid {

    ObjectNode bidder;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ExtImpPrebidBuilder {}
}
