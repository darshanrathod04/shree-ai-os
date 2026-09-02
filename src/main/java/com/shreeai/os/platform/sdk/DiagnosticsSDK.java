package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.services.SdkDiagnosticsService;

import java.util.Map;
import java.util.Objects;

/**
 * <b>DiagnosticsSDK</b>
 *
 * <p>SDK façade for runtime diagnostics. Exposed via {@code shree.diagnostics()}.</p>
 *
 * <p><b>Ownership:</b> Platform Services (v1.0)</p>
 *
 * @since v1.0
 */
public final class DiagnosticsSDK {

    private final SdkDiagnosticsService service;

    public DiagnosticsSDK(SdkDiagnosticsService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    /**
     * Returns the full diagnostics report.
     */
    public Map<String, Object> report() {
        return service.report();
    }

    /**
     * Returns a formatted multi-line report.
     */
    public String reportAsString() {
        return service.reportAsString();
    }

    /**
     * Configures the active provider.
     */
    public DiagnosticsSDK provider(String name) {
        service.provider(name);
        return this;
    }

    /**
     * Configures the active model.
     */
    public DiagnosticsSDK model(String name) {
        service.model(name);
        return this;
    }

    /**
     * Configures the active kernel.
     */
    public DiagnosticsSDK kernel(SdkDiagnosticsService.Kernel kernel) {
        service.activeKernel(kernel);
        return this;
    }
}
