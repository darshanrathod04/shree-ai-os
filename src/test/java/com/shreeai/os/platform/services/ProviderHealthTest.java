package com.shreeai.os.platform.services;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>ProviderHealthTest</b>
 *
 * <p>10 test cases for ProviderHealthService.</p>
 *
 * @since v1.0
 */
public class ProviderHealthTest {

    private final ByokSettingsService settingsService = new ByokSettingsService();
    private final ProviderHealthService healthService = new ProviderHealthService(settingsService);

    @Test
    void health_returnsAllProviderSnapshots() {
        List<Map<String, Object>> report = healthService.health();
        assertEquals(3, report.size());
        assertTrue(report.stream().anyMatch(m -> "OPENAI".equals(m.get("provider"))));
        assertTrue(report.stream().anyMatch(m -> "GEMINI".equals(m.get("provider"))));
        assertTrue(report.stream().anyMatch(m -> "OLLAMA".equals(m.get("provider"))));
    }

    @Test
    void snapshot_includesLatencyDisplay() {
        Map<String, Object> snapshot = healthService.snapshot(ProviderType.OPENAI);
        assertTrue(snapshot.containsKey("latencyDisplay"));
        assertNotNull(snapshot.get("status"));
    }

    @Test
    void healthReport_formatted() {
        String report = healthService.healthReport();
        assertNotNull(report);
        assertFalse(report.isEmpty());
    }

    @Test
    void lastLatency_returnsNegativeIfNeverChecked() {
        long latency = healthService.lastLatency(ProviderType.GEMINI);
        assertEquals(-1, latency);
    }

    @Test
    void lastStatus_returnsNullIfNeverChecked() {
        var status = healthService.lastStatus(ProviderType.OPENAI);
        assertNull(status);
    }

    @Test
    void checkOpenAI_returnsHealthStatus() {
        var status = healthService.checkOpenAI();
        assertNotNull(status);
        // Could be HEALTHY, DEGRADED, or OFFLINE
        assertTrue(
                status == ProviderHealthService.HealthStatus.HEALTHY ||
                status == ProviderHealthService.HealthStatus.DEGRADED ||
                status == ProviderHealthService.HealthStatus.OFFLINE
        );
    }

    @Test
    void checkGemini_returnsHealthStatus() {
        var status = healthService.checkGemini();
        assertNotNull(status);
    }

    @Test
    void checkOllama_returnsHealthStatus() {
        var status = healthService.checkOllama();
        assertNotNull(status);
    }

    @Test
    void reset_clearsLatencyAndStatus() {
        healthService.checkOpenAI();
        healthService.reset();
        assertEquals(-1, healthService.lastLatency(ProviderType.OPENAI));
        assertNull(healthService.lastStatus(ProviderType.OPENAI));
    }

    @Test
    void snapshot_neverChecked_returnsOffline() {
        Map<String, Object> snapshot = healthService.snapshot(ProviderType.GEMINI);
        assertEquals("OFFLINE", snapshot.get("status"));
        assertEquals("-", snapshot.get("latencyDisplay"));
    }
}
