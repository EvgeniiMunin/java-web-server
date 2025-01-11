package org.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder(toBuilder = true)
@Value
@JsonDeserialize(builder = BidRequest.BidRequestBuilder.class)
public class BidRequest {

    String id;

    List<Imp> imp;

    Site site;

    @JsonPOJOBuilder(withPrefix = "")
    public static class BidRequestBuilder {}
}
