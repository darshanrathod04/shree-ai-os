package com.shreeai.os.platform.services;

import com.shreeai.os.platform.kernels.knowledge.evaluation.RagEvaluator;
import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.sdk.streaming.StreamingListener;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>PlaygroundController</b> — Unified REST surface for {@code shree-platform-web}
 * SDK Playground ({@code /api/playground/*}).
 *
 * <p>Exposes Chat, Identity, Memory, Knowledge, Planning, Execution, and Reflection
 * endpoints directly on the main Spring Boot server so the Next.js frontend works
 * out of the box against a single backend port.</p>
 */
@RestController
@RequestMapping("/api/playground")
public class PlaygroundController {

    private static final int MAX_INPUT_CHARS = 100_000;

    private final ShreeAI ai;
    private final RagEvaluator ragEvaluator = new RagEvaluator();

    public PlaygroundController(ShreeAI ai) {
        this.ai = Objects.requireNonNull(ai, "ai must not be null");
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents() {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of(
                            "status", "connected",
                            "timestamp", Instant.now().toString()
                    )));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> body) {
        String message = str(body, "message");
        if (message.length() > MAX_INPUT_CHARS) {
            return ResponseEntity.badRequest().body(Map.of("error", "message exceeds maximum length"));
        }
        var trace = ai.tracer().startTrace("playground.chat", "default");
        long t0 = System.nanoTime();
        ResponseEntity<?> response = handle(() -> ai.chat(message));
        trace.addSpan("sdk-chat", "llm", (System.nanoTime() - t0) / 1_000_000L,
                Map.of("status", response.getStatusCode().value()));
        trace.finish(response.getStatusCode().is2xxSuccessful() ? "OK" : "ERROR");
        return response;
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, Object> body) {
        String message = str(body, "message");
        SseEmitter emitter = new SseEmitter(120_000L);
        if (message.isBlank() || message.length() > MAX_INPUT_CHARS) {
            try {
                emitter.send(SseEmitter.event().name("error").data(Map.of("error", "Valid message required")));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }
        ai.chatStream(message, new StreamingListener() {
            @Override
            public void onStart() {
                sendQuietly(emitter, "start", Map.of("status", "streaming"));
            }

            @Override
            public void onToken(String token) {
                sendQuietly(emitter, "token", Map.of("token", token));
            }

            @Override
            public void onComplete(String completeResponse) {
                sendQuietly(emitter, "complete", Map.of(
                        "answer", completeResponse == null ? "" : completeResponse
                ));
                emitter.complete();
            }

            @Override
            public void onError(Throwable error) {
                sendQuietly(emitter, "error", Map.of("error", error.getMessage() == null ? "Stream error" : error.getMessage()));
                emitter.completeWithError(error);
            }
        });
        return emitter;
    }

    private static void sendQuietly(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (Exception ignored) {
            // Client disconnected
        }
    }

    @PostMapping("/identity/get")
    public ResponseEntity<?> getIdentity(@RequestBody Map<String, Object> body) {
        return handle(() -> ai.identity().getIdentity(str(body, "identityId")));
    }

    @PostMapping("/identity/create")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> createIdentity(@RequestBody Map<String, Object> body) {
        Map<String, Object> profile = body.get("profile") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : Map.of();
        return handle(() -> ai.identity().createIdentity(
                str(body, "identityId"),
                str(body, "identityType"),
                profile));
    }

    @PostMapping("/memory/store")
    public ResponseEntity<?> memoryStore(@RequestBody Map<String, Object> body) {
        return handle(() -> ai.memory().store(str(body, "title"), str(body, "content")));
    }

    @PostMapping("/memory/search")
    public ResponseEntity<?> memorySearch(@RequestBody Map<String, Object> body) {
        return handle(() -> ai.memory().search(str(body, "query")));
    }

    @PostMapping("/memory/recall")
    public ResponseEntity<?> memoryRecall(@RequestBody Map<String, Object> body) {
        return handle(() -> ai.memory().recall(str(body, "query")));
    }

    @PostMapping("/knowledge/search")
    public ResponseEntity<?> knowledgeSearch(@RequestBody Map<String, Object> body) {
        return handle(() -> ai.knowledge().search(str(body, "query")));
    }

    @PostMapping("/knowledge/query")
    public ResponseEntity<?> knowledgeQuery(@RequestBody Map<String, Object> body) {
        return handle(() -> ai.knowledge().query(str(body, "query")));
    }

    @PostMapping("/knowledge/ingest")
    public ResponseEntity<?> knowledgeIngest(@RequestBody Map<String, Object> body) {
        return handle(() -> ai.knowledge().ingest(str(body, "title"), str(body, "content")));
    }

    @PostMapping("/planning/create")
    public ResponseEntity<?> createPlan(@RequestBody Map<String, Object> body) {
        return handle(() -> ai.planning().createPlan(
                str(body, "objectiveId"),
                str(body, "objective"),
                str(body, "scope")));
    }

    @PostMapping("/planning/refine")
    public ResponseEntity<?> refinePlan(@RequestBody Map<String, Object> body) {
        return handle(() -> ai.planning().refinePlan(
                str(body, "planId"),
                str(body, "refinement")));
    }

    @PostMapping("/planning/validate")
    public ResponseEntity<?> validatePlan(@RequestBody Map<String, Object> body) {
        return handle(() -> ai.planning().validatePlan(str(body, "planId")));
    }

    @PostMapping("/execution")
    public ResponseEntity<?> execute(@RequestBody Map<String, Object> body) {
        return handle(() -> ai.execution().execute(str(body, "capability"), str(body, "input")));
    }

    @PostMapping("/execution/verify")
    public ResponseEntity<?> verifyExecution(@RequestBody Map<String, Object> body) {
        return handle(() -> ai.execution().verify(str(body, "executionId")));
    }

    @PostMapping("/reflection/run")
    public ResponseEntity<?> reflect(@RequestBody Map<String, Object> body) {
        return handle(() -> ai.reflection().reflect(str(body, "executionId")));
    }

    @PostMapping("/reflection/history")
    public ResponseEntity<?> reflectionHistory(@RequestBody Map<String, Object> body) {
        int limit = body.get("limit") instanceof Number n ? n.intValue() : 10;
        return handle(() -> ai.reflection().getHistory(str(body, "tenantId"), limit));
    }

    @PostMapping("/reflection/analytics")
    public ResponseEntity<?> reflectionAnalytics(@RequestBody Map<String, Object> body) {
        int window = body.get("window") instanceof Number n ? n.intValue() : 10;
        return handle(() -> ai.reflection().getAnalytics(str(body, "tenantId"), window));
    }

    @PostMapping("/agent")
    public ResponseEntity<?> runAgent(@RequestBody Map<String, Object> body) {
        String goal = str(body, "goal");
        if (goal.isBlank() || goal.length() > MAX_INPUT_CHARS) {
            return ResponseEntity.badRequest().body(Map.of("error", "Valid goal is required"));
        }
        String model = str(body, "model");
        int maxSteps = body.get("maxSteps") instanceof Number n ? n.intValue() : 5;
        return handle(() -> ai.agent().run(model, goal, maxSteps));
    }

    @GetMapping("/telemetry")
    public ResponseEntity<?> telemetry() {
        return ResponseEntity.ok(Map.of(
                "providers", ai.agent().routerTelemetry(),
                "registeredTools", ai.agent().registeredTools()
        ));
    }

    @GetMapping("/traces")
    public ResponseEntity<?> traces() {
        return ResponseEntity.ok(Map.of(
                "traces", ai.tracer().recent(50)
        ));
    }

    @PostMapping("/rag/evaluate")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> evaluateRag(@RequestBody Map<String, Object> body) {
        String query = str(body, "query");
        String answer = str(body, "answer");
        java.util.List<String> contexts = body.get("contexts") instanceof java.util.List<?> list
                ? list.stream().map(String::valueOf).toList()
                : java.util.List.of();
        if (query.isBlank() || answer.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "query and answer are required"));
        }
        return ResponseEntity.ok(ragEvaluator.evaluate(query, contexts, answer));
    }

    private ResponseEntity<?> handle(java.util.function.Supplier<SDKResponse> action) {
        try {
            SDKResponse res = action.get();
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("answer", res.answer() == null ? "" : res.answer());
            json.put("confidence", res.confidence());
            json.put("reasoningAvailable", res.reasoningAvailable());
            json.put("metadata", res.metadata() == null ? "" : res.metadata());
            json.put("structuredPayload", res.structuredPayload() == null ? Map.of() : res.structuredPayload());
            json.put("timestamp", (res.timestamp() == null ? Instant.now() : res.timestamp()).toString());
            return ResponseEntity.ok(json);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    private static String str(Map<String, Object> body, String key) {
        if (body == null || body.get(key) == null) {
            return "";
        }
        return String.valueOf(body.get(key));
    }
}
