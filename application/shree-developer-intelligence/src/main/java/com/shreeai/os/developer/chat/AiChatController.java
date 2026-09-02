package com.shreeai.os.developer.chat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <b>AiChatController</b> — REST surface for Module 2: AI Chat.
 *
 * <p><b>Endpoints:</b></p>
 * <pre>
 * POST /api/developer/chat/ask      — ask a question about the project
 * POST /api/developer/chat/remember  — store a project memory
 * POST /api/developer/chat/recall   — recall memories for a query
 * </pre>
 *
 * <p>All endpoints require a workspace session ID. Responses are
 * grounded in the analyzed project via {@code KnowledgeSDK} and enriched
 * with workspace-scoped memory via {@code MemorySDK}.</p>
 */
@RestController
@RequestMapping("/api/developer/chat")
@CrossOrigin(origins = "*")
public class AiChatController {

    private final AiChatService chatService;

    public AiChatController(AiChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Ask a natural language question about the project.
     *
     * <p>Examples:
     * <ul>
     *   <li>"Explain the UserService class"</li>
     *   <li>"Where is authentication implemented?"</li>
     *   <li>"Which APIs modify Order?"</li>
     *   <li>"Find circular dependencies"</li>
     * </ul>
     */
    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody AskRequest request) {
        if (request == null) {
            return bad("Request body required");
        }
        if (request.sessionId() == null || request.sessionId().isBlank()) {
            return bad("sessionId is required");
        }
        if (request.question() == null || request.question().isBlank()) {
            return bad("question is required");
        }
        ChatResponse response = chatService.ask(request.sessionId(), request.question());
        return ResponseEntity.ok(response.toMap());
    }

    /**
     * Store a project-specific memory entry (coding conventions, architecture
     * decisions, team preferences, naming rules).
     *
     * <p>Memory is workspace-scoped and persisted for the application run.</p>
     */
    @PostMapping("/remember")
    public ResponseEntity<?> remember(@RequestBody RememberRequest request) {
        if (request == null) {
            return bad("Request body required");
        }
        if (request.sessionId() == null || request.sessionId().isBlank()) {
            return bad("sessionId is required");
        }
        if (request.title() == null || request.title().isBlank()) {
            return bad("title is required");
        }
        chatService.remember(request.sessionId(), request.title(), request.content());
        return ResponseEntity.ok(Map.of(
                "status", "stored",
                "sessionId", request.sessionId(),
                "title", request.title()
        ));
    }

    /**
     * Recall memories relevant to a query within a workspace.
     */
    @PostMapping("/recall")
    public ResponseEntity<?> recall(@RequestBody RecallRequest request) {
        if (request == null) {
            return bad("Request body required");
        }
        if (request.sessionId() == null || request.sessionId().isBlank()) {
            return bad("sessionId is required");
        }
        var response = chatService.recall(request.sessionId(), request.query());
        return ResponseEntity.ok(Map.of(
                "answer", response.answer() != null ? response.answer() : "",
                "confidence", response.confidence()
        ));
    }

    private ResponseEntity<Map<String, String>> bad(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    // Request DTOs

    public record AskRequest(String sessionId, String question) {}
    public record RememberRequest(String sessionId, String title, String content) {}
    public record RecallRequest(String sessionId, String query) {}
}
