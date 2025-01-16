package org.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.request.BidRequest;
import org.request.ExtImpPrebid;
import org.request.Imp;
import org.request.InferenceMessage;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class InferenceDataService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public BidRequest parseBidRequest(String filePath) throws IOException {
        return objectMapper.readValue(new File(filePath), BidRequest.class);
    }

    public List<InferenceMessage> extractInferenceMessages(BidRequest bidRequest) {
        final ZonedDateTime timestamp = ZonedDateTime.now(ZoneId.of("UTC"));
        final Integer hourBucket = timestamp.getHour();
        final Integer minuteQuadrant = (timestamp.getMinute() / 15) + 1;

        final String hostname = bidRequest.getSite().getPage();
        final List<Imp> imps = bidRequest.getImp();

        return imps.stream()
                .map(imp -> extractMessagesForImp(
                        imp,
                        hostname,
                        hourBucket,
                        minuteQuadrant))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<InferenceMessage> extractMessagesForImp(
            Imp imp,
            String hostname,
            Integer hourBucket,
            Integer minuteQuadrant) {

        final String impId = imp.getId();
        final ObjectNode impExt = imp.getExt();
        final JsonNode bidderNode = extImpPrebid(impExt.get("prebid")).getBidder();

        return createInferenceMessages(bidderNode, impId, hostname, hourBucket, minuteQuadrant);
    }

    private List<InferenceMessage> createInferenceMessages(
            JsonNode bidderNode,
            String impId,
            String hostname,
            Integer hourBucket,
            Integer minuteQuadrant) {

        final List<InferenceMessage> inferenceImpMessages = new ArrayList<>();

        if (!bidderNode.isObject()) {
            return inferenceImpMessages;
        }

        final ObjectNode bidders = (ObjectNode) bidderNode;
        final Iterator<String> fieldNames = bidders.fieldNames();
        while (fieldNames.hasNext()) {
            final String bidderName = fieldNames.next();
            inferenceImpMessages.add(buildInferenceMessage(
                    bidderName,
                    impId,
                    hostname,
                    hourBucket,
                    minuteQuadrant));
        }

        return inferenceImpMessages;
    }

    private InferenceMessage buildInferenceMessage(
            String bidderName,
            String impId,
            String hostname,
            Integer hourBucket,
            Integer minuteQuadrant) {

        return InferenceMessage.builder()
                .bidder(bidderName)
                .adUnitCode(impId)
                .hostname(hostname)
                .hourBucket(hourBucket.toString())
                .minuteQuadrant(minuteQuadrant.toString())
                .build();
    }

    private ExtImpPrebid extImpPrebid(JsonNode extImpPrebid) {
        try {
            return objectMapper.treeToValue(extImpPrebid, ExtImpPrebid.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error decoding imp.ext.prebid: " + e.getMessage(), e);
        }
    }
}
