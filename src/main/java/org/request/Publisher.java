package org.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
@JsonDeserialize(builder = Publisher.PublisherBuilder.class)
public class Publisher {

    String id;

    @JsonPOJOBuilder(withPrefix = "")
    public static class PublisherBuilder {}
}
