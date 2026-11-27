package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.sdk.observability.GenAiTracer;
import com.shreeai.os.platform.sdk.structured.StructuredOutputConverter;
import com.shreeai.os.platform.services.PlaygroundController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class GlobalMilestone1Test {

    public record SentimentReport(String sentiment, double score, List<String> keywords) {}

    @Test
    @DisplayName("StructuredOutputConverter deserializes Java records and auto-retries malformed JSON")
    void testStructuredOutputWithRetry() {
        AtomicInteger calls = new AtomicInteger(0);
        SentimentReport report = StructuredOutputConverter.convertWithRetry(
                "Analyze: Shree AI OS is fast and reliable.",
                SentimentReport.class,
                prompt -> {
                    if (calls.getAndIncrement() == 0) {
                        return "not valid json {";
                    }
                    return """
                            ```json
                            {"sentiment": "POSITIVE", "score": 0.95, "keywords": ["fast", "reliable"]}
                            ```
                            """;
                }
        );

        assertEquals(2, calls.get());
        assertEquals("POSITIVE", report.sentiment());
        assertEquals(0.95, report.score(), 0.001);
        assertEquals(List.of("fast", "reliable"), report.keywords());
    }

    @Test
    @DisplayName("GenAiTracer records span trees and PlaygroundController exposes /traces")
    @SuppressWarnings("unchecked")
    void testGenAiTracerAndPlaygroundTracesEndpoint() {
        try (ShreeAI ai = ShreeAI.builder().apiKey("local").build()) {
            PlaygroundController controller = new PlaygroundController(ai);
            ResponseEntity<?> chatRes = controller.chat(Map.of("message", "Hello telemetry"));
            assertEquals(200, chatRes.getStatusCode().value());

            ResponseEntity<?> tracesRes = controller.traces();
            assertEquals(200, tracesRes.getStatusCode().value());
            Map<String, Object> body = (Map<String, Object>) tracesRes.getBody();
            assertNotNull(body);
            List<GenAiTracer.TraceRecord> traces = (List<GenAiTracer.TraceRecord>) body.get("traces");
            assertFalse(traces.isEmpty());
            assertEquals("playground.chat", traces.get(traces.size() - 1).operation());
            assertEquals(1, traces.get(traces.size() - 1).spans().size());
        }
    }
}
