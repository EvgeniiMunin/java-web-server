package org.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
@JsonDeserialize(builder = Site.SiteBuilder.class)
public class Site {

    Publisher publisher;

    String page;

    @JsonPOJOBuilder(withPrefix = "")
    public static class SiteBuilder {}
}
