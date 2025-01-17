package org.server;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.ResourceAttributes;

public class TelemetryService {

    Resource resource;

    SdkMeterProvider sdkMeterProvider;

    OpenTelemetry openTelemetry;

    public TelemetryService(String OtlpHttpMetricExporterEndpoint) {
        resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(
                        ResourceAttributes.SERVICE_NAME, "rtb-prediction-service")));

        sdkMeterProvider = SdkMeterProvider.builder()
                .setResource(resource)
                .registerMetricReader(PeriodicMetricReader.builder(
                                OtlpHttpMetricExporter.builder()
                                        .setEndpoint(OtlpHttpMetricExporterEndpoint)
                                        .build())
                        .build())
                .build();

        openTelemetry = OpenTelemetrySdk.builder()
                .setMeterProvider(sdkMeterProvider)
                .buildAndRegisterGlobal();
    }

    public Meter meter() {
        return openTelemetry
                .meterBuilder("rtb-prediction-service-meter-builder")
                .setInstrumentationVersion("1.0.0")
                .build();
    }
}
