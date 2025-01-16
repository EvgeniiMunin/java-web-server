package org.server;

public class GlobalTelemetryTestContext {

    private static final TelemetryService TELEMETRY_CONFIG = new TelemetryService(
            "http://otel-collector:4318/v1/traces");

    public static TelemetryService getTelemetryConfig() {
        return TELEMETRY_CONFIG;
    }
}
