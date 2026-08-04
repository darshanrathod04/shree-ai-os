package com.shreeai.os.platform.execution;

import com.shreeai.os.platform.cognition.CognitiveDecision;
import com.shreeai.os.platform.validation.ValidationResult;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable execution context helper.
 *
 * <p>This class provides a context object that bundles together the decision,
 * validation result, execution request, and future execution information.
 * It is passed to capabilities during execution to provide full context.</p>
 *
 * <p>This class is thread-safe and immutable by design.
 * All fields are final and set via constructor or builder.</p>
 *
 * <p>This is part of the stable execution contract (ABI) for Shree AI OS.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 6.1
 */
public final class ExecutionContext {

    private final CognitiveDecision decision;
    private final ValidationResult validationResult;
    private final ExecutionRequest executionRequest;
    private final String parentExecutionId;
    private final int retryCount;
    private final Map<String, Object> futureExecutionInfo;
    private final Instant timestamp;

    /**
     * Create ExecutionContext with builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Private constructor - use builder for construction.
     */
    private ExecutionContext(
            CognitiveDecision decision,
            ValidationResult validationResult,
            ExecutionRequest executionRequest,
            String parentExecutionId,
            int retryCount,
            Map<String, Object> futureExecutionInfo,
            Instant timestamp
    ) {
        this.decision = decision;
        this.validationResult = validationResult;
        this.executionRequest = executionRequest;
        this.parentExecutionId = parentExecutionId;
        this.retryCount = retryCount;
        this.futureExecutionInfo = futureExecutionInfo != null
                ? Collections.unmodifiableMap(new HashMap<>(futureExecutionInfo))
                : Collections.emptyMap();
        this.timestamp = timestamp;
    }

    // Getters
    public CognitiveDecision getDecision() {
        return decision;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }

    public ExecutionRequest getExecutionRequest() {
        return executionRequest;
    }

    public String getParentExecutionId() {
        return parentExecutionId;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Map<String, Object> getFutureExecutionInfo() {
        return futureExecutionInfo;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Check if this is a retry execution.
     *
     * @return true if this is a retry
     */
    public boolean isRetry() {
        return retryCount > 0;
    }

    /**
     * Check if this execution has a parent execution.
     *
     * @return true if this execution has a parent
     */
    public boolean hasParent() {
        return parentExecutionId != null && !parentExecutionId.isBlank();
    }

    /**
     * Get a future execution info value by key.
     *
     * @param key the key
     * @return the value or null if not present
     */
    public Object getFutureExecutionInfo(String key) {
        return futureExecutionInfo.get(key);
    }

    /**
     * Get a future execution info value by key with type casting.
     *
     * @param key the key
     * @param type the expected type
     * @return the value cast to the type, or null if not present
     */
    @SuppressWarnings("unchecked")
    public <T> T getFutureExecutionInfo(String key, Class<T> type) {
        Object value = futureExecutionInfo.get(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new ClassCastException(
                    "Future execution info for key '" + key + "' is not of type " + type.getName()
            );
        }
        return (T) value;
    }

    /**
     * Check if a future execution info key exists.
     *
     * @param key the key
     * @return true if the key exists
     */
    public boolean hasFutureExecutionInfo(String key) {
        return futureExecutionInfo.containsKey(key);
    }

    @Override
    public String toString() {
        return "ExecutionContext{" +
                "decision=" + decision +
                ", validationResult=" + (validationResult != null ? validationResult.getValidationId() : "null") +
                ", executionRequest=" + (executionRequest != null ? executionRequest.getRequestId() : "null") +
                ", parentExecutionId='" + parentExecutionId + '\'' +
                ", retryCount=" + retryCount +
                ", futureExecutionInfo=" + futureExecutionInfo +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExecutionContext that = (ExecutionContext) o;

        if (retryCount != that.retryCount) return false;
        if (decision != null ? !decision.equals(that.decision) : that.decision != null) return false;
        if (validationResult != null ? !validationResult.equals(that.validationResult) : that.validationResult != null)
            return false;
        if (executionRequest != null ? !executionRequest.equals(that.executionRequest) : that.executionRequest != null)
            return false;
        if (parentExecutionId != null ? !parentExecutionId.equals(that.parentExecutionId) : that.parentExecutionId != null)
            return false;
        if (!futureExecutionInfo.equals(that.futureExecutionInfo)) return false;
        return timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        int result = decision != null ? decision.hashCode() : 0;
        result = 31 * result + (validationResult != null ? validationResult.hashCode() : 0);
        result = 31 * result + (executionRequest != null ? executionRequest.hashCode() : 0);
        result = 31 * result + (parentExecutionId != null ? parentExecutionId.hashCode() : 0);
        result = 31 * result + retryCount;
        result = 31 * result + futureExecutionInfo.hashCode();
        result = 31 * result + timestamp.hashCode();
        return result;
    }

    /**
     * Builder for ExecutionContext.
     */
    public static class Builder {
        private CognitiveDecision decision;
        private ValidationResult validationResult;
        private ExecutionRequest executionRequest;
        private String parentExecutionId;
        private int retryCount;
        private Map<String, Object> futureExecutionInfo = new HashMap<>();
        private Instant timestamp = Instant.now();

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
         * Set the parent execution ID for nested/retry executions.
         *
         * @param parentExecutionId the parent execution ID
         * @return this builder
         */
        public Builder parentExecutionId(String parentExecutionId) {
            this.parentExecutionId = parentExecutionId;
            return this;
        }

        /**
         * Set the retry count.
         *
         * @param retryCount the retry count
         * @return this builder
         */
        public Builder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        /**
         * Add future execution information.
         *
         * @param key the key
         * @param value the value
         * @return this builder
         */
        public Builder addFutureExecutionInfo(String key, Object value) {
            this.futureExecutionInfo.put(key, value);
            return this;
        }

        /**
         * Set all future execution info (replaces existing map).
         *
         * @param futureExecutionInfo the future execution info map
         * @return this builder
         */
        public Builder futureExecutionInfo(Map<String, Object> futureExecutionInfo) {
            this.futureExecutionInfo = futureExecutionInfo != null
                    ? new HashMap<>(futureExecutionInfo)
                    : new HashMap<>();
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
         * Build the ExecutionContext instance.
         *
         * @return a new ExecutionContext instance
         */
        public ExecutionContext build() {
            return new ExecutionContext(
                    decision, validationResult, executionRequest,
                    parentExecutionId, retryCount, futureExecutionInfo, timestamp
            );
        }
    }
}