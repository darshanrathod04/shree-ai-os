package com.shreeai.os.platform.runtime.pipeline;

import com.shreeai.os.platform.legacy.cognition.CognitiveDecision;
import com.shreeai.os.platform.legacy.execution.ExecutionRequest;
import com.shreeai.os.platform.legacy.execution.ExecutionMetadata;
import com.shreeai.os.platform.legacy.production.ResolvedContext;
import com.shreeai.os.platform.validation.ValidationResult;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable pipeline context.
 *
 * <p>This class contains all the information needed for pipeline execution.
 * It is passed through all stages and can be enriched by each stage.</p>
 *
 * <p>This class is thread-safe and immutable by design.
 * All fields are final and set via constructor or builder.</p>
 *
 * <p>This is part of the stable Runtime Pipeline contract for Shree AI OS.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 *
 * @since Sprint 6.2A
 */
public final class PipelineContext {

    private final String pipelineId;
    private final ExecutionRequest executionRequest;
    private final CognitiveDecision decision;
    private final ValidationResult validationResult;
    private final ExecutionMetadata executionMetadata;
    private final ResolvedContext resolvedContext;
    private final Map<String, Object> attributes;
    private final Instant timestamp;

    /**
     * Create PipelineContext with builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Private constructor - use builder for construction.
     */
    private PipelineContext(
            String pipelineId,
            ExecutionRequest executionRequest,
            CognitiveDecision decision,
            ValidationResult validationResult,
            ExecutionMetadata executionMetadata,
            ResolvedContext resolvedContext,
            Map<String, Object> attributes,
            Instant timestamp
    ) {
        this.pipelineId = pipelineId;
        this.executionRequest = executionRequest;
        this.decision = decision;
        this.validationResult = validationResult;
        this.executionMetadata = executionMetadata;
        this.resolvedContext = resolvedContext;
        this.attributes = attributes != null
                ? Collections.unmodifiableMap(new HashMap<>(attributes))
                : Collections.emptyMap();
        this.timestamp = timestamp;
    }

    // Getters
    public String getPipelineId() {
        return pipelineId;
    }

    public ExecutionRequest getExecutionRequest() {
        return executionRequest;
    }

    public CognitiveDecision getDecision() {
        return decision;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }

    public ExecutionMetadata getExecutionMetadata() {
        return executionMetadata;
    }

    public ResolvedContext getResolvedContext() {
        return resolvedContext;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Get an attribute by key.
     *
     * @param key the attribute key
     * @return the value or null if not present
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Get an attribute by key with type casting.
     *
     * @param key the attribute key
     * @param type the expected type
     * @return the value cast to the type, or null if not present
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new ClassCastException(
                    "Attribute for key '" + key + "' is not of type " + type.getName()
            );
        }
        return (T) value;
    }

    /**
     * Check if an attribute exists.
     *
     * @param key the attribute key
     * @return true if the key exists
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    @Override
    public String toString() {
        return "PipelineContext{" +
                "pipelineId='" + pipelineId + '\'' +
                ", executionRequest=" + (executionRequest != null ? executionRequest.getRequestId() : "null") +
                ", decision=" + decision +
                ", validationResult=" + (validationResult != null ? validationResult.getValidationId() : "null") +
                ", executionMetadata=" + (executionMetadata != null ? executionMetadata.getExecutionId() : "null") +
                ", resolvedContext=" + resolvedContext +
                ", attributes=" + attributes +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PipelineContext that = (PipelineContext) o;

        if (!pipelineId.equals(that.pipelineId)) return false;
        if (executionRequest != null ? !executionRequest.equals(that.executionRequest) : that.executionRequest != null)
            return false;
        if (decision != null ? !decision.equals(that.decision) : that.decision != null) return false;
        if (validationResult != null ? !validationResult.equals(that.validationResult) : that.validationResult != null)
            return false;
        if (executionMetadata != null ? !executionMetadata.equals(that.executionMetadata) : that.executionMetadata != null)
            return false;
        if (resolvedContext != null ? !resolvedContext.equals(that.resolvedContext) : that.resolvedContext != null)
            return false;
        if (!attributes.equals(that.attributes)) return false;
        return timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        int result = pipelineId.hashCode();
        result = 31 * result + (executionRequest != null ? executionRequest.hashCode() : 0);
        result = 31 * result + (decision != null ? decision.hashCode() : 0);
        result = 31 * result + (validationResult != null ? validationResult.hashCode() : 0);
        result = 31 * result + (executionMetadata != null ? executionMetadata.hashCode() : 0);
        result = 31 * result + (resolvedContext != null ? resolvedContext.hashCode() : 0);
        result = 31 * result + attributes.hashCode();
        result = 31 * result + timestamp.hashCode();
        return result;
    }

    /**
     * Builder for PipelineContext.
     */
    public static class Builder {
        private String pipelineId = UUID.randomUUID().toString();
        private ExecutionRequest executionRequest;
        private CognitiveDecision decision;
        private ValidationResult validationResult;
        private ExecutionMetadata executionMetadata;
        private ResolvedContext resolvedContext;
        private Map<String, Object> attributes = new HashMap<>();
        private Instant timestamp = Instant.now();

        /**
         * Set the pipeline ID (defaults to new UUID if not set).
         *
         * @param pipelineId the pipeline ID
         * @return this builder
         */
        public Builder pipelineId(String pipelineId) {
            this.pipelineId = pipelineId;
            return this;
        }

        /**
         * Set the execution request.
         *
         * @param executionRequest the execution request
         * @return this builder
         */
        public Builder executionRequest(ExecutionRequest executionRequest) {
            this.executionRequest = executionRequest;
            return this;
        }

        /**
         * Set the cognitive decision.
         *
         * @param decision the cognitive decision
         * @return this builder
         */
        public Builder decision(CognitiveDecision decision) {
            this.decision = decision;
            return this;
        }

        /**
         * Set the validation result.
         *
         * @param validationResult the validation result
         * @return this builder
         */
        public Builder validationResult(ValidationResult validationResult) {
            this.validationResult = validationResult;
            return this;
        }

        /**
         * Set the execution metadata.
         *
         * @param executionMetadata the execution metadata
         * @return this builder
         */
        public Builder executionMetadata(ExecutionMetadata executionMetadata) {
            this.executionMetadata = executionMetadata;
            return this;
        }

        /**
         * Set the resolved context.
         *
         * @param resolvedContext the resolved context
         * @return this builder
         */
        public Builder resolvedContext(ResolvedContext resolvedContext) {
            this.resolvedContext = resolvedContext;
            return this;
        }

        /**
         * Add an attribute to the context.
         *
         * @param key the attribute key
         * @param value the attribute value
         * @return this builder
         */
        public Builder addAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        /**
         * Set all attributes (replaces existing map).
         *
         * @param attributes the attributes map
         * @return this builder
         */
        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
            return this;
        }

        /**
         * Set the timestamp (defaults to now if not set).
         *
         * @param timestamp the timestamp
         * @return this builder
         */
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Build the PipelineContext instance.
         *
         * @return a new PipelineContext instance
         */
        public PipelineContext build() {
            return new PipelineContext(
                    pipelineId, executionRequest, decision, validationResult,
                    executionMetadata, resolvedContext, attributes, timestamp
            );
        }
    }
}