package org.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder(toBuilder = true)
@Value
@JsonDeserialize(builder = Banner.BannerBuilder.class)
public class Banner {

    List<Format> format;

    @JsonPOJOBuilder(withPrefix = "")
    public static class BannerBuilder {}
}
