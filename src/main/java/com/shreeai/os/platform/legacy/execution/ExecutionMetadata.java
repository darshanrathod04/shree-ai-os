package com.shreeai.os.platform.legacy.execution;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable metadata model for execution tracking.
 *
 * <p>This class provides execution metadata including source information,
 * tracing identifiers, and custom key-value pairs for future expansion.</p>
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
public final class ExecutionMetadata {

    private final String executionSource;
    private final String traceId;
    private final String sessionId;
    private final String executionId;
    private final Map<String, Object> customValues;
    private final Instant timestamp;

    /**
     * Create ExecutionMetadata with builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Private constructor - use builder for construction.
     */
    private ExecutionMetadata(
            String executionSource,
            String traceId,
            String sessionId,
            String executionId,
            Map<String, Object> customValues,
            Instant timestamp
    ) {
        this.executionSource = executionSource;
        this.traceId = traceId;
        this.sessionId = sessionId;
        this.executionId = executionId;
        this.customValues = customValues != null
                ? Collections.unmodifiableMap(new HashMap<>(customValues))
                : Collections.emptyMap();
        this.timestamp = timestamp;
    }

    // Getters
    public String getExecutionSource() {
        return executionSource;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public Map<String, Object> getCustomValues() {
        return customValues;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Get a custom value by key.
     *
     * @param key the custom key
     * @return the value or null if not present
     */
    public Object getCustomValue(String key) {
        return customValues.get(key);
    }

    /**
     * Get a custom value by key with type casting.
     *
     * @param key the custom key
     * @param type the expected type
     * @return the value cast to the type, or null if not present
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomValue(String key, Class<T> type) {
        Object value = customValues.get(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new ClassCastException(
                    "Custom value for key '" + key + "' is not of type " + type.getName()
            );
        }
        return (T) value;
    }

    /**
     * Check if a custom key exists.
     *
     * @param key the custom key
     * @return true if the key exists
     */
    public boolean hasCustomValue(String key) {
        return customValues.containsKey(key);
    }

    @Override
    public String toString() {
        return "ExecutionMetadata{" +
                "executionSource='" + executionSource + '\'' +
                ", traceId='" + traceId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", executionId='" + executionId + '\'' +
                ", customValues=" + customValues +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExecutionMetadata that = (ExecutionMetadata) o;

        if (executionSource != null ? !executionSource.equals(that.executionSource) : that.executionSource != null)
            return false;
        if (traceId != null ? !traceId.equals(that.traceId) : that.traceId != null) return false;
        if (sessionId != null ? !sessionId.equals(that.sessionId) : that.sessionId != null) return false;
        if (executionId != null ? !executionId.equals(that.executionId) : that.executionId != null) return false;
        if (!customValues.equals(that.customValues)) return false;
        return timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        int result = executionSource != null ? executionSource.hashCode() : 0;
        result = 31 * result + (traceId != null ? traceId.hashCode() : 0);
        result = 31 * result + (sessionId != null ? sessionId.hashCode() : 0);
        result = 31 * result + (executionId != null ? executionId.hashCode() : 0);
        result = 31 * result + customValues.hashCode();
        result = 31 * result + timestamp.hashCode();
        return result;
    }

    /**
     * Builder for ExecutionMetadata.
     */
    public static class Builder {
        private String executionSource;
        private String traceId = UUID.randomUUID().toString();
        private String sessionId;
        private String executionId = UUID.randomUUID().toString();
        private Map<String, Object> customValues = new HashMap<>();
        private Instant timestamp = Instant.now();

        /**
         * Set the execution source (e.g., "AgentBrain", "SkillRouter", "API").
         *
         * @param executionSource the execution source
         * @return this builder
         */
        public Builder executionSource(String executionSource) {
            this.executionSource = executionSource;
            return this;
        }

        /**
         * Set the trace ID for distributed tracing.
         *
         * @param traceId the trace ID
         * @return this builder
         */
        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        /**
         * Set the session ID.
         *
         * @param sessionId the session ID
         * @return this builder
         */
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        /**
         * Set the execution ID (defaults to new UUID if not set).
         *
         * @param executionId the execution ID
         * @return this builder
         */
        public Builder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        /**
         * Add a custom key-value pair.
         *
         * @param key the custom key
         * @param value the custom value
         * @return this builder
         */
        public Builder addCustomValue(String key, Object value) {
            this.customValues.put(key, value);
            return this;
        }

        /**
         * Set all custom values (replaces existing map).
         *
         * @param customValues the custom values map
         * @return this builder
         */
        public Builder customValues(Map<String, Object> customValues) {
            this.customValues = customValues != null ? new HashMap<>(customValues) : new HashMap<>();
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
         * Build the ExecutionMetadata instance.
         *
         * @return a new ExecutionMetadata instance
         */
        public ExecutionMetadata build() {
            return new ExecutionMetadata(
                    executionSource, traceId, sessionId, executionId, customValues, timestamp
            );
        }
    }
}