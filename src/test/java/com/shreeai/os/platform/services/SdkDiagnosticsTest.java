package com.shreeai.os.platform.services;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>SdkDiagnosticsTest</b>
 *
 * <p>8 test cases for SdkDiagnosticsService.</p>
 *
 * @since v1.0
 */
public class SdkDiagnosticsTest {

    private final SdkDiagnosticsService service = new SdkDiagnosticsService();

    @Test
    void report_containsAllFields() {
        Map<String, Object> report = service.report();
        assertTrue(report.containsKey("provider"));
        assertTrue(report.containsKey("model"));
        assertTrue(report.containsKey("kernel"));
        assertTrue(report.containsKey("latencyMs"));
        assertTrue(report.containsKey("knowledgeHits"));
        assertTrue(report.containsKey("memoryUsed"));
        assertTrue(report.containsKey("routingSource"));
    }

    @Test
    void report_defaultValues() {
        Map<String, Object> report = service.report();
        assertEquals("in-memory", report.get("provider"));
        assertEquals("default", report.get("model"));
        assertTrue(report.get("kernel").toString().contains("KNOWLEDGE Kernel"));
        assertEquals(0L, report.get("latencyMs"));
        assertEquals(0, report.get("knowledgeHits"));
        assertEquals(true, report.get("memoryUsed"));
    }

    @Test
    void recordKnowledgeHit_incrementsCount() {
        service.recordKnowledgeHit();
        service.recordKnowledgeHit();
        service.recordKnowledgeHit();
        assertEquals(3, service.report().get("knowledgeHits"));
    }

    @Test
    void startTimer_measuresLatency() {
        Runnable stop = service.startTimer();
        // Simulate some work
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        stop.run();
        assertTrue(service.latencyMs() >= 5);
    }

    @Test
    void reset_clearsRuntimeMetrics() {
        service.recordKnowledgeHit();
        service.latencyMs(100);
        service.memoryUsed(false);
        service.reset();
        assertEquals(0, service.report().get("knowledgeHits"));
        assertEquals(0L, service.report().get("latencyMs"));
        assertEquals(true, service.report().get("memoryUsed"));
    }

    @Test
    void fluentSetters_work() {
        service.provider("openai").model("gpt-5").activeKernel(SdkDiagnosticsService.Kernel.MEMORY);
        Map<String, Object> report = service.report();
        assertEquals("openai", report.get("provider"));
        assertEquals("gpt-5", report.get("model"));
        assertTrue(report.get("kernel").toString().contains("MEMORY Kernel"));
    }

    @Test
    void reportAsString_isMultiLine() {
        String report = service.reportAsString();
        assertNotNull(report);
        assertTrue(report.lines().count() > 1);
    }

    @Test
    void routingSource_setCorrectly() {
        service.routingSource(SdkDiagnosticsService.RoutingSource.LLM_ROUTER);
        assertEquals("LLM_ROUTER", service.report().get("routingSource"));
        service.routingSource(SdkDiagnosticsService.RoutingSource.SDK);
        assertEquals("SDK", service.report().get("routingSource"));
    }
}
