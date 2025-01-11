package org.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
@JsonDeserialize(builder = Imp.ImpBuilder.class)
public class Imp {

    String id;

    Banner banner;

    ObjectNode ext;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ImpBuilder {}
}
