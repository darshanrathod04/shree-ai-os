package com.shreeai.os.platform.services;

import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@CrossOrigin(origins = "*")
public class PlaygroundController {

    private final ShreeAI ai;

    public PlaygroundController(ShreeAI ai) {
        this.ai = Objects.requireNonNull(ai, "ai must not be null");
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> body) {
        return handle(() -> ai.chat(str(body, "message")));
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
