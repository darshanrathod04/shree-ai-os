package com.shreeai.os.platform.services;

import com.shreeai.os.platform.sdk.SDKRequest;
import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

/**
 * <b>AiChatController</b> — REST surface for the main ShreeAiOsApplication.
 *
 * <p>Wraps {@link ShreeAI#chat(SDKRequest)} so that trust validation
 * scenarios can hit the platform's Chief intelligence pipeline over HTTP
 * using the {@code /api/v1/chat} path.</p>
 *
 * <p><b>Endpoints:</b></p>
 * <pre>
 * POST /api/v1/chat   — send a user message to the platform
 * </pre>
 *
 * <p><b>Request body:</b></p>
 * <pre>
 * {
 *   "message": "Explain WorkspaceController",
 *   "userId":  "trust-1"
 * }
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Services (Sprint 19 Trust Validation)</p>
 */
@RestController
@RequestMapping("/api/v1/chat")
public class AiChatController {

    private final ShreeAI ai;

    public AiChatController(ShreeAI ai) {
        this.ai = Objects.requireNonNull(ai, "ai");
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body required"));
        }
        if (request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "message is required"));
        }
        try {
            SDKRequest sdk = SDKRequest.builder()
                    .message(request.message())
                    .build();
            SDKResponse response = ai.chat(sdk);
            Map<String, Object> payload = response.structuredPayload();
            return ResponseEntity.ok(Map.of(
                    "answer", response.answer(),
                    "confidence", response.confidence(),
                    "userId", request.userId() == null ? "anonymous" : request.userId(),
                    "structuredPayload", payload == null ? Map.of() : payload
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    public record ChatRequest(String message, String userId) {}
}
