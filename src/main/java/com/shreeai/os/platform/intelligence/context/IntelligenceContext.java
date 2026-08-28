package com.shreeai.os.platform.intelligence.context;

import com.shreeai.os.platform.intelligence.evidence.EvidenceItem;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>IntelligenceContext</b>
 *
 * <p>The central structured context aggregate for the Shree AI OS intelligence
 * pipeline.</p>
 *
 * <p>IntelligenceContext carries the complete situational awareness for a single
 * intelligence operation: the structured request, project profile, evidence items,
 * memory references, knowledge references, environment information, and metadata.
 * It is immutable and designed to preserve information end-to-end without
 * flattening structured data into strings.</p>
 *
 * <p>This aggregate composes with the existing Context Kernel models
 * ({@code ExecutionContext}, {@code SessionContext}, {@code ConversationContext})
 * rather than duplicating them. It adds the intelligence-specific concepts
 * (evidence, provenance, project profile, relevance) that the existing generic
 * {@code Map<String,Object>} context data cannot represent.</p>
 *
 * <p><b>Ownership:</b> Intelligence Foundation</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param contextId the unique context identifier (must not be null or blank)
 * @param request the structured intelligence request (must not be null)
 * @param project the structured project profile (may be {@code null})
 * @param evidence the evidence items available to this operation (defensively copied)
 * @param memoryReferences identifiers of relevant memories (defensively copied)
 * @param knowledgeReferences identifiers of relevant knowledge nodes (defensively copied)
 * @param environment the environment information (defensively copied)
 * @param metadata additional context metadata (defensively copied)
 * @param createdAt the context creation timestamp (must not be null)
 */
public record IntelligenceContext(
        String contextId,
        IntelligenceRequest request,
        ProjectProfile project,
        List<EvidenceItem> evidence,
        List<String> memoryReferences,
        List<String> knowledgeReferences,
        Map<String, Object> environment,
        Map<String, Object> metadata,
        Instant createdAt
) {

    /**
     * Creates a new IntelligenceContext with validation.
     *
     * @throws NullPointerException if contextId, request, or createdAt is null
     * @throws IllegalArgumentException if contextId is blank
     */
    public IntelligenceContext {
        Objects.requireNonNull(contextId, "contextId must not be null");
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        if (contextId.isBlank()) {
            throw new IllegalArgumentException("contextId must not be blank");
        }
        evidence = evidence != null ? List.copyOf(evidence) : List.of();
        memoryReferences = memoryReferences != null ? List.copyOf(memoryReferences) : List.of();
        knowledgeReferences = knowledgeReferences != null ? List.copyOf(knowledgeReferences) : List.of();
        environment = environment != null ? Map.copyOf(environment) : Map.of();
        metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    /**
     * Returns a builder for IntelligenceContext.
     *
     * @param contextId the context identifier
     * @param request the structured request
     * @return a new builder
     */
    public static Builder builder(String contextId, IntelligenceRequest request) {
        return new Builder(contextId, request);
    }

    /**
     * Fluent builder for IntelligenceContext.
     */
    public static final class Builder {
        private final String contextId;
        private final IntelligenceRequest request;
        private ProjectProfile project;
        private List<EvidenceItem> evidence = List.of();
        private List<String> memoryReferences = List.of();
        private List<String> knowledgeReferences = List.of();
        private Map<String, Object> environment = Map.of();
        private Map<String, Object> metadata = Map.of();
        private Instant createdAt = Instant.now();

        private Builder(String contextId, IntelligenceRequest request) {
            this.contextId = Objects.requireNonNull(contextId, "contextId must not be null");
            this.request = Objects.requireNonNull(request, "request must not be null");
        }

        public Builder project(ProjectProfile project) {
            this.project = project;
            return this;
        }

        public Builder evidence(List<EvidenceItem> evidence) {
            this.evidence = evidence != null ? List.copyOf(evidence) : List.of();
            return this;
        }

        public Builder memoryReferences(List<String> memoryReferences) {
            this.memoryReferences = memoryReferences != null ? List.copyOf(memoryReferences) : List.of();
            return this;
        }

        public Builder knowledgeReferences(List<String> knowledgeReferences) {
            this.knowledgeReferences = knowledgeReferences != null ? List.copyOf(knowledgeReferences) : List.of();
            return this;
        }

        public Builder environment(Map<String, Object> environment) {
            this.environment = environment != null ? Map.copyOf(environment) : Map.of();
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
            return this;
        }

        public IntelligenceContext build() {
            return new IntelligenceContext(
                    contextId, request, project, evidence,
                    memoryReferences, knowledgeReferences,
                    environment, metadata, createdAt
            );
        }
    }
}