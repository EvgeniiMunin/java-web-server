package org.server;

import ai.onnxruntime.OrtException;
import org.request.BidRequest;
import org.request.InferenceMessage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Application {
    public static void main(String[] args) throws IOException, OrtException {

        InferenceDataService inferenceDataService = new InferenceDataService();
        final BidRequest bidRequest = inferenceDataService.parseBidRequest(
                "src/main/resources/bid_request2.json");

        final OnnxModelRunner onnxModelRunner = new OnnxModelRunner(
                "src/main/resources/models_pbuid=test-pbuid.onnx");

        System.out.println(
                "Application/main \n" +
                        "onnxModelRunner: " + onnxModelRunner + "\n"
        );

        final List<InferenceMessage> inferenceMessages = inferenceDataService
                .extractInferenceMessages(bidRequest);

        System.out.println(
                "Application/main \n" +
                        "inferenceMessages: " + inferenceMessages + "\n"
        );

        final FilterService filterService = new FilterService();
        final Map<String, Map<String, Double>> predictions = filterService.predictBids(
                onnxModelRunner, inferenceMessages);

        System.out.println(
                "Application/main \n" +
                        "predictions: " + predictions + "\n"
        );

        // SpringApplication.run(Application.class, args);
        // Micronaut.run(Application.class, args);
    }
}
