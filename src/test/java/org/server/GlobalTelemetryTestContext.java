package org.server;

public class GlobalTelemetryTestContext {

    private static final TelemetryConfig TELEMETRY_CONFIG = new TelemetryConfig();

    public static TelemetryConfig getTelemetryConfig() {
        return TELEMETRY_CONFIG;
    }
}
