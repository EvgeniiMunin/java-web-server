package org.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.request.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InferenceDataServiceTest {

    private InferenceDataService target;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        target = new InferenceDataService();
    }

    @Test
    void parseBidRequestShouldFailIfFileDoesNotExist() {
        assertThrows(Exception.class, () ->
                target.parseBidRequest("non_existent_file.json")
        );
    }

    @Test
    void testExtractInferenceMessages() {
        // given
        BidRequest bidRequest = givenBidRequest();
        List<InferenceMessage> expectedInferenceMessages = expectedInferenceMessages();

        // when
        List<InferenceMessage> result = target.extractInferenceMessages(bidRequest);

        // then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (int i = 0; i < expectedInferenceMessages.size(); i++) {
            InferenceMessage expected = expectedInferenceMessages.get(i);
            InferenceMessage actual = result.get(i);

            assertEquals(expected.getBidder(), actual.getBidder());
            assertEquals(expected.getAdUnitCode(), actual.getAdUnitCode());
            assertEquals(expected.getHostname(), actual.getHostname());
        }
    }

    private BidRequest givenBidRequest() {
        // Banner formats
        Format format1 = Format.builder().w(300).h(250).build();
        Format format2 = Format.builder().w(300).h(600).build();
        Banner banner = Banner.builder()
                .format(Arrays.asList(format1, format2))
                .build();

        // Build ext with "prebid.bidder.rubicon" and "prebid.bidder.appnexus"
        // Typically, 'ext' is a JSON tree. We'll use Jackson's ObjectNode for simplicity:
        // something like: { "prebid": { "bidder": { "rubicon": {...}, "appnexus": {...} } } }
        var extNode = mapper.createObjectNode();
        var prebidNode = extNode.putObject("prebid");
        var bidderNode = prebidNode.putObject("bidder");

        var rubiconNode = bidderNode.putObject("rubicon");
        rubiconNode.put("accountId", 1001);
        rubiconNode.put("siteId", 267318);
        rubiconNode.put("zoneId", 1861698);

        var appnexusNode = bidderNode.putObject("appnexus");
        appnexusNode.put("placementId", 123456);

        // Now the Imp
        Imp imp = Imp.builder()
                .id("pub_banniere_haute")
                .ext(extNode)   // Attach that JSON
                .banner(banner)
                .build();

        // Publisher and Site
        Publisher publisher = Publisher.builder()
                .id("1001")
                .build();

        Site site = Site.builder()
                .publisher(publisher)
                .page("http://example.com/prebid_server_test.html")
                .build();

        // Finally the BidRequest
        return BidRequest.builder()
                .id("1")
                .imp(Arrays.asList(imp))
                .site(site)
                .build();
    }

    private List<InferenceMessage> expectedInferenceMessages() {
        InferenceMessage rubiconMsg = InferenceMessage.builder()
                .bidder("rubicon")
                .adUnitCode("pub_banniere_haute")
                .hostname("http://example.com/prebid_server_test.html")
                // hourBucket and minuteQuadrant are dynamic, so we won't set them
                .build();

        InferenceMessage appnexusMsg = InferenceMessage.builder()
                .bidder("appnexus")
                .adUnitCode("pub_banniere_haute")
                .hostname("http://example.com/prebid_server_test.html")
                .build();

        return Arrays.asList(rubiconMsg, appnexusMsg);
    }
}
