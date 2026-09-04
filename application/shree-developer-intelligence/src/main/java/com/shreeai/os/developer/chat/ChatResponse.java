package com.shreeai.os.developer.chat;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ChatResponse</b> — AI chat result for the developer assistant.
 *
 * <p>Combines the knowledge response with project memory to give a
 * grounded, context-aware answer.</p>
 */
public final class ChatResponse {

    private final String sessionId;
    private final String question;
    private final String answer;
    private final double confidence;
    private final boolean knowledgeUsed;
    private final boolean memoryUsed;
    private final boolean projectIntelligenceUsed;   // Sprint-17.3
    private final Instant timestamp;

    private ChatResponse(Builder b) {
        this.sessionId = Objects.requireNonNull(b.sessionId);
        this.question = Objects.requireNonNull(b.question);
        this.answer = b.answer == null ? "" : b.answer;
        this.confidence = Math.max(0.0, Math.min(1.0, b.confidence));
        this.knowledgeUsed = b.knowledgeUsed;
        this.memoryUsed = b.memoryUsed;
        this.projectIntelligenceUsed = b.projectIntelligenceUsed;   // Sprint-17.3
        this.timestamp = b.timestamp == null ? Instant.now() : b.timestamp;
    }

    public String sessionId() { return sessionId; }
    public String question() { return question; }
    public String answer() { return answer; }
    public double confidence() { return confidence; }
    public boolean knowledgeUsed() { return knowledgeUsed; }
    public boolean memoryUsed() { return memoryUsed; }
    public boolean projectIntelligenceUsed() { return projectIntelligenceUsed; }   // Sprint-17.3
    public Instant timestamp() { return timestamp; }

    public Map<String, Object> toMap() {
        return Map.of(
                "sessionId", sessionId,
                "question", question,
                "answer", answer,
                "confidence", confidence,
                "knowledgeUsed", knowledgeUsed,
                "memoryUsed", memoryUsed,
                "projectIntelligenceUsed", projectIntelligenceUsed,   // Sprint-17.3
                "timestamp", timestamp.toString()
        );
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String sessionId;
        private String question;
        private String answer;
        private double confidence = 0.5;
        private boolean knowledgeUsed = false;
        private boolean memoryUsed = false;
        private boolean projectIntelligenceUsed = false;   // Sprint-17.3
        private Instant timestamp;

        public Builder sessionId(String v) { this.sessionId = v; return this; }
        public Builder question(String v) { this.question = v; return this; }
        public Builder answer(String v) { this.answer = v; return this; }
        public Builder confidence(double v) { this.confidence = v; return this; }
        public Builder knowledgeUsed(boolean v) { this.knowledgeUsed = v; return this; }
        public Builder memoryUsed(boolean v) { this.memoryUsed = v; return this; }
        public Builder projectIntelligenceUsed(boolean v) { this.projectIntelligenceUsed = v; return this; }   // Sprint-17.3
        public Builder timestamp(Instant v) { this.timestamp = v; return this; }

        public ChatResponse build() { return new ChatResponse(this); }
    }
}
