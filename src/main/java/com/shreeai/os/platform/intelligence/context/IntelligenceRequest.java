package com.shreeai.os.platform.intelligence.context;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>IntelligenceRequest</b>
 *
 * <p>Structured request representation for the Shree AI OS intelligence pipeline.</p>
 *
 * <p>This replaces the current practice of passing only a raw {@code String} user
 * input through the pipeline. An IntelligenceRequest carries the user message,
 * intent, objective, constraints, identity, session, and project evidence as
 * first-class structured fields — none of which are flattened into strings.</p>
 *
 * <p><b>Ownership:</b> Intelligence Foundation</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param requestId the unique request identifier (must not be null or blank)
 * @param userInput the raw user message (must not be null)
 * @param intent the classified intent of the request (may be {@code null} if unknown)
 * @param objective the reasoning/planning objective (may be {@code null})
 * @param constraints the request constraints (defensively copied)
 * @param userId the user identifier (may be {@code null})
 * @param sessionId the session identifier (may be {@code null})
 * @param project the structured project profile (may be {@code null})
 * @param evidence the evidence items supplied with this request (defensively copied)
 * @param metadata additional request metadata (defensively copied)
 * @param requestedAt the request timestamp (must not be null)
 */
public record IntelligenceRequest(
        String requestId,
        String userInput,
        String intent,
        String objective,
        Map<String, Object> constraints,
        String userId,
        String sessionId,
        ProjectProfile project,
        List<com.shreeai.os.platform.intelligence.evidence.EvidenceItem> evidence,
        Map<String, Object> metadata,
        Instant requestedAt
) {

    /**
     * Creates a new IntelligenceRequest with validation.
     *
     * @throws NullPointerException if requestId, userInput, or requestedAt is null
     * @throws IllegalArgumentException if requestId is blank
     */
    public IntelligenceRequest {
        Objects.requireNonNull(requestId, "requestId must not be null");
        Objects.requireNonNull(userInput, "userInput must not be null");
        Objects.requireNonNull(requestedAt, "requestedAt must not be null");
        if (requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        constraints = constraints != null ? Map.copyOf(constraints) : Map.of();
        evidence = evidence != null ? List.copyOf(evidence) : List.of();
        metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    /**
     * Returns a builder for IntelligenceRequest.
     *
     * @param requestId the request identifier
     * @param userInput the user message
     * @return a new builder
     */
    public static Builder builder(String requestId, String userInput) {
        return new Builder(requestId, userInput);
    }

    /**
     * Fluent builder for IntelligenceRequest.
     */
    public static final class Builder {
        private final String requestId;
        private final String userInput;
        private String intent;
        private String objective;
        private Map<String, Object> constraints = Map.of();
        private String userId;
        private String sessionId;
        private ProjectProfile project;
        private List<com.shreeai.os.platform.intelligence.evidence.EvidenceItem> evidence = List.of();
        private Map<String, Object> metadata = Map.of();
        private Instant requestedAt = Instant.now();

        private Builder(String requestId, String userInput) {
            this.requestId = Objects.requireNonNull(requestId, "requestId must not be null");
            this.userInput = Objects.requireNonNull(userInput, "userInput must not be null");
        }

        public Builder intent(String intent) {
            this.intent = intent;
            return this;
        }

        public Builder objective(String objective) {
            this.objective = objective;
            return this;
        }

        public Builder constraints(Map<String, Object> constraints) {
            this.constraints = constraints != null ? Map.copyOf(constraints) : Map.of();
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder project(ProjectProfile project) {
            this.project = project;
            return this;
        }

        public Builder evidence(List<com.shreeai.os.platform.intelligence.evidence.EvidenceItem> evidence) {
            this.evidence = evidence != null ? List.copyOf(evidence) : List.of();
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
            return this;
        }

        public Builder requestedAt(Instant requestedAt) {
            this.requestedAt = Objects.requireNonNull(requestedAt, "requestedAt must not be null");
            return this;
        }

        public IntelligenceRequest build() {
            return new IntelligenceRequest(
                    requestId, userInput, intent, objective, constraints,
                    userId, sessionId, project, evidence, metadata, requestedAt
            );
        }
    }
}