package org.server;

import ai.onnxruntime.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.request.InferenceMessage;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PredictionServiceTest {

    private PredictionService target;

    @Mock
    private OnnxModelRunner onnxModelRunnerMock;

    @BeforeEach
    void setUp() {
        target = new PredictionService(GlobalTelemetryTestContext.getTelemetryConfig());
    }

    @Test
    @DisplayName("predictBids() should return valid probas map when OnnxModelRunner returns data")
    void predictBidsShouldReturnValidProbasMap() throws OrtException {
        // given
        final List<InferenceMessage> inferenceMessages = givenInferenceMessages();
        final OnnxModelRunner onnxModelRunner = givenOnnxModelRunner();

        // when
        final var result = target.predictBids(onnxModelRunner, inferenceMessages);

        System.out.println(
                "PredictionServiceTest/predictBidsShouldReturnValidProbasMap \n" +
                        "result: " + result + "\n"
        );

        // then
        assertEquals(0.3, result.get("impId_1").get("bidderA"), 1e-5);
    }

    @Test
    @DisplayName("predictBids() should throw RuntimeException if inferenceMessages is null or empty")
    void testPredictBidsEmptyMessages() {
        assertThrows(RuntimeException.class, () ->
                target.predictBids(onnxModelRunnerMock, null)
        );

        assertThrows(RuntimeException.class, () ->
                target.predictBids(onnxModelRunnerMock, Collections.emptyList())
        );
    }

    private OnnxModelRunner givenOnnxModelRunner() throws OrtException {
        return new OnnxModelRunner("src/test/resources/models_pbuid=test-pbuid.onnx");
    }

    private List<InferenceMessage> givenInferenceMessages() {
        final InferenceMessage inferenceMessage = InferenceMessage.builder()
                .bidder("bidderA")
                .adUnitCode("impId_1")
                .hostname("example.com")
                .hourBucket("10")
                .minuteQuadrant("1")
                .build();

        return Arrays.asList(inferenceMessage);
    }
}
