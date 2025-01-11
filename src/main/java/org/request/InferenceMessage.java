package org.request;

import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
public class InferenceMessage {

    String browser;

    String bidder;

    String adUnitCode;

    String country;

    String hostname;

    String device;

    String hourBucket;

    String minuteQuadrant;
}
