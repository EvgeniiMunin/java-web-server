package org.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
@JsonDeserialize(builder = Format.FormatBuilder.class)
public class Format {

    Integer w;

    Integer h;

    @JsonPOJOBuilder(withPrefix = "")
    public static class FormatBuilder {}
}
