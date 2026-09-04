package com.shreeai.os.platform.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * <b>SdkDiagnosticsController</b>
 *
 * <p>REST controller for the SDK diagnostics endpoint.</p>
 *
 * <p><b>Endpoint:</b> {@code GET /api/sdk/diagnostics}</p>
 *
 * <p><b>Ownership:</b> Platform Services (v1.0)</p>
 */
@RestController
@RequestMapping("/api/sdk/diagnostics")
public class SdkDiagnosticsController {

    private final SdkDiagnosticsService diagnostics;

    public SdkDiagnosticsController(SdkDiagnosticsService diagnostics) {
        this.diagnostics = diagnostics;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> diagnostics() {
        return ResponseEntity.ok(diagnostics.report());
    }
}
