package org.server;

import ai.onnxruntime.OrtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {

    @Value("${webapp.modelPath}")
    private String modelPath;

    @Value("${otel-collector.endpoint}")
    private String OtlpHttpMetricExporterEndpoint;

    @Bean
    public InferenceDataService inferenceDataService() {
        return new InferenceDataService();
    }

    @Bean
    public OnnxModelRunner onnxModelRunner() throws OrtException {
        return new OnnxModelRunner(modelPath);
    }

    @Bean
    public TelemetryService telemetryService(String OtlpHttpMetricExporterEndpoint) {
        return new TelemetryService(OtlpHttpMetricExporterEndpoint);
    }

    @Bean
    public PredictionService predictionService(TelemetryService telemetryService) {
        return new PredictionService(telemetryService);
    }
}
