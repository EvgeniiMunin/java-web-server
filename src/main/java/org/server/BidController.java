package org.server;

import org.request.BidRequest;
import org.request.InferenceMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class BidController {

    private final InferenceDataService inferenceDataService;

    private final OnnxModelRunner onnxModelRunner;

    private final PredictionService predictionService;

    public BidController(
            InferenceDataService inferenceDataService,
            OnnxModelRunner onnxModelRunner,
            PredictionService predictionService) {
        this.inferenceDataService = Objects.requireNonNull(inferenceDataService);
        this.onnxModelRunner = Objects.requireNonNull(onnxModelRunner);
        this.predictionService = Objects.requireNonNull(predictionService);
    }

    @GetMapping("/healthz")
    public ResponseEntity<String> healthz() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/bid")
    public ResponseEntity<?> bid(@RequestBody BidRequest bidRequest) {
        final List<InferenceMessage> inferenceMessages = inferenceDataService
                .extractInferenceMessages(bidRequest);

        final Map<String, Map<String, Double>> predictions = predictionService.predictBids(
                onnxModelRunner, inferenceMessages);

        return ResponseEntity.ok(predictions);
    }
}
