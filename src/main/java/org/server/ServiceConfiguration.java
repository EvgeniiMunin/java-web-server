package org.server;

import ai.onnxruntime.OrtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {

    @Value("${webapp.modelPath}")
    private String modelPath;

    @Bean
    public InferenceDataService inferenceDataService() {
        return new InferenceDataService();
    }

    @Bean
    public OnnxModelRunner onnxModelRunner() throws OrtException {

        System.out.println(
                "ServiceConfiguration/onnxModelRunner \n" +
                        "modelPath: " + modelPath + "\n"
        );

        return new OnnxModelRunner(modelPath);
    }

    @Bean
    public TelemetryConfig telemetryConfig() {
        return new TelemetryConfig();
    }

    @Bean
    public PredictionService predictionService(TelemetryConfig telemetryConfig) {

        System.out.println(
                "ServiceConfiguration/predictionService \n" +
                        "telemetryConfig: " + telemetryConfig + "\n"
        );

        return new PredictionService(telemetryConfig);
    }
}
