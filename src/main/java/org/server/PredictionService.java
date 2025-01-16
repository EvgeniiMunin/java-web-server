package org.server;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import org.request.InferenceMessage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PredictionService {

    Meter meter;

    DoubleHistogram predictionsHistogram;

    public PredictionService(TelemetryService telemetryService) {
        meter = telemetryService.meter();
        predictionsHistogram = meter
                .histogramBuilder("rtb.predictions")
                .setDescription("Bid prediction values")
                .build();
    }

    public Map<String, Map<String, Double>> predictBids(
            OnnxModelRunner onnxModelRunner, List<InferenceMessage> inferenceMessages) {
        long startMillis = System.nanoTime();
        final OrtSession.Result results;
        try {
            final String[][] inferenceRows = convertToArray(inferenceMessages);
            results = onnxModelRunner.runModel(inferenceRows);
            return processModelResults(results, inferenceMessages);
        } catch (OrtException e) {
            throw new RuntimeException("Exception during model inference: ", e);
        } finally {
            long endMillis = System.nanoTime();
            double durationMillis = (endMillis - startMillis) / 1000000.0;
        }
    }

    private static String[][] convertToArray(List<InferenceMessage> messages) {
        return messages.stream()
                .map(message -> new String[]{
                        defaultIfNull(message.getBrowser()),
                        defaultIfNull(message.getBidder()),
                        defaultIfNull(message.getAdUnitCode()),
                        defaultIfNull(message.getCountry()),
                        defaultIfNull(message.getHostname()),
                        defaultIfNull(message.getDevice()),
                        defaultIfNull(message.getHourBucket()),
                        defaultIfNull(message.getMinuteQuadrant())})
                .toArray(String[][]::new);
    }

    private static String defaultIfNull(String value) {
        return value == null ? "" : value;
    }

    private Map<String, Map<String, Double>> processModelResults(
            OrtSession.Result results,
            List<InferenceMessage> inferenceMessages) {

        validateInferenceMessages(inferenceMessages);

        return StreamSupport.stream(results.spliterator(), false)
                .peek(PredictionService::validateOnnxTensor)
                .filter(onnxItem -> Objects.equals(onnxItem.getKey(), "probabilities"))
                .map(Map.Entry::getValue)
                .map(OnnxTensor.class::cast)
                .peek(tensor -> validateTensorSize(tensor, inferenceMessages.size()))
                .map(tensor -> extractAndProcessProbabilities(tensor, inferenceMessages))
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static void validateInferenceMessages(List<InferenceMessage> throttlingMessages) {
        if (throttlingMessages == null || throttlingMessages.isEmpty()) {
            throw new RuntimeException("throttlingMessages cannot be null or empty");
        }
    }

    private static void validateOnnxTensor(Map.Entry<String, OnnxValue> onnxItem) {
        if (!(onnxItem.getValue() instanceof OnnxTensor)) {
            throw new RuntimeException("Expected OnnxTensor for 'probabilities', but found: "
                    + onnxItem.getValue().getClass().getName());
        }
    }

    private static void validateTensorSize(OnnxTensor tensor, int expectedSize) {
        final long[] tensorShape = tensor.getInfo().getShape();
        if (tensorShape.length == 0 || tensorShape[0] != expectedSize) {
            throw new RuntimeException("Mismatch between tensor size and throttlingMessages size");
        }
    }

    private Map<String, Map<String, Double>> extractAndProcessProbabilities(
            OnnxTensor tensor,
            List<InferenceMessage> inferenceMessages) {

        try {
            final float[][] probabilities = extractProbabilitiesValues(tensor);
            Map<String, Map<String, Double>> processedProbabilities = processProbabilities(probabilities, inferenceMessages);
            processedProbabilities.forEach(this::recordPredictions);

            return processedProbabilities;
        } catch (OrtException e) {
            throw new RuntimeException("Exception when extracting proba from OnnxTensor: ", e);
        }
    }

    private float[][] extractProbabilitiesValues(OnnxTensor tensor) throws OrtException {
        return (float[][]) tensor.getValue();
    }

    private Map<String, Map<String, Double>> processProbabilities(
            float[][] probabilities,
            List<InferenceMessage> throttlingMessages) {

        final Map<String, Map<String, Double>> result = new HashMap<>();

        for (int i = 0; i < probabilities.length; i++) {
            final InferenceMessage message = throttlingMessages.get(i);
            final String impId = message.getAdUnitCode();
            final String bidder = message.getBidder();
            result.computeIfAbsent(impId, k -> new HashMap<>()).put(bidder, (double) probabilities[i][1]);
        }

        return result;
    }

    public void recordPredictions(String impId, Map<String, Double> predictions) {
        predictions.forEach((bidder, prediction) -> {
            predictionsHistogram.record(
                    prediction,
                    Attributes.of(
                            AttributeKey.stringKey("impId"), impId,
                            AttributeKey.stringKey("bidder"), bidder));
        });
    }
}
