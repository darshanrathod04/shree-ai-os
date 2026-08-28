package com.shreeai.os.platform.legacy.execution;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable execution result model.
 *
 * <p>This class represents the result of a capability execution.
 * It contains the execution status, response data, error information,
 * and performance metrics.</p>
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
public final class ExecutionResult {

    private final String executionId;
    private final String requestId;
    private final boolean success;
    private final ExecutionStatus status;
    private final String response;
    private final String errorMessage;
    private final long executionTime;
    private final String capabilityName;
    private final ExecutionMetadata metadata;
    private final Instant timestamp;

    /**
     * Create ExecutionResult with builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Private constructor - use builder for construction.
     */
    private ExecutionResult(
            String executionId,
            String requestId,
            boolean success,
            ExecutionStatus status,
            String response,
            String errorMessage,
            long executionTime,
            String capabilityName,
            ExecutionMetadata metadata,
            Instant timestamp
    ) {
        this.executionId = executionId;
        this.requestId = requestId;
        this.success = success;
        this.status = status;
        this.response = response;
        this.errorMessage = errorMessage;
        this.executionTime = executionTime;
        this.capabilityName = capabilityName;
        this.metadata = metadata;
        this.timestamp = timestamp;
    }

    // Getters
    public String getExecutionId() {
        return executionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public String getResponse() {
        return response;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public String getCapabilityName() {
        return capabilityName;
    }

    public ExecutionMetadata getMetadata() {
        return metadata;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Check if the execution failed.
     *
     * @return true if the execution failed
     */
    public boolean isFailed() {
        return !success;
    }

    /**
     * Check if the execution has an error message.
     *
     * @return true if an error message is present
     */
    public boolean hasError() {
        return errorMessage != null && !errorMessage.isBlank();
    }

    @Override
    public String toString() {
        return "ExecutionResult{" +
                "executionId='" + executionId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", success=" + success +
                ", status=" + status +
                ", response='" + (response != null ? response.substring(0, Math.min(50, response.length())) : "null") + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", executionTime=" + executionTime + "ms" +
                ", capabilityName='" + capabilityName + '\'' +
                ", metadata=" + metadata +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExecutionResult that = (ExecutionResult) o;

        if (success != that.success) return false;
        if (executionTime != that.executionTime) return false;
        if (!executionId.equals(that.executionId)) return false;
        if (!requestId.equals(that.requestId)) return false;
        if (status != that.status) return false;
        if (response != null ? !response.equals(that.response) : that.response != null) return false;
        if (errorMessage != null ? !errorMessage.equals(that.errorMessage) : that.errorMessage != null) return false;
        if (!capabilityName.equals(that.capabilityName)) return false;
        if (!metadata.equals(that.metadata)) return false;
        return timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        int result = executionId.hashCode();
        result = 31 * result + requestId.hashCode();
        result = 31 * result + (success ? 1 : 0);
        result = 31 * result + status.hashCode();
        result = 31 * result + (response != null ? response.hashCode() : 0);
        result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
        result = 31 * result + (int) (executionTime ^ (executionTime >>> 32));
        result = 31 * result + capabilityName.hashCode();
        result = 31 * result + metadata.hashCode();
        result = 31 * result + timestamp.hashCode();
        return result;
    }

    /**
     * Builder for ExecutionResult.
     */
    public static class Builder {
        private String executionId = UUID.randomUUID().toString();
        private String requestId;
        private boolean success;
        private ExecutionStatus status = ExecutionStatus.UNKNOWN;
        private String response;
        private String errorMessage;
        private long executionTime;
        private String capabilityName;
        private ExecutionMetadata metadata;
        private Instant timestamp = Instant.now();

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
         * Set the request ID (required).
         *
         * @param requestId the request ID
         * @return this builder
         */
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        /**
         * Set the success flag.
         *
         * @param success true if execution was successful
         * @return this builder
         */
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        /**
         * Set the execution status.
         *
         * @param status the execution status
         * @return this builder
         */
        public Builder status(ExecutionStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Set the response data.
         *
         * @param response the response data
         * @return this builder
         */
        public Builder response(String response) {
            this.response = response;
            return this;
        }

        /**
         * Set the error message.
         *
         * @param errorMessage the error message
         * @return this builder
         */
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        /**
         * Set the execution time in milliseconds.
         *
         * @param executionTime the execution time
         * @return this builder
         */
        public Builder executionTime(long executionTime) {
            this.executionTime = executionTime;
            return this;
        }

        /**
         * Set the capability name (required).
         *
         * @param capabilityName the capability name
         * @return this builder
         */
        public Builder capabilityName(String capabilityName) {
            this.capabilityName = capabilityName;
            return this;
        }

        /**
         * Set the execution metadata.
         *
         * @param metadata the execution metadata
         * @return this builder
         */
        public Builder metadata(ExecutionMetadata metadata) {
            this.metadata = metadata;
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
         * Build the ExecutionResult instance.
         *
         * @return a new ExecutionResult instance
         * @throws IllegalStateException if required fields are missing
         */
        public ExecutionResult build() {
            validateRequiredFields();
            return new ExecutionResult(
                    executionId, requestId, success, status, response,
                    errorMessage, executionTime, capabilityName, metadata, timestamp
            );
        }

        /**
         * Validate that all required fields are present.
         *
         * @throws IllegalStateException if any required field is missing
         */
        private void validateRequiredFields() {
            if (requestId == null || requestId.isBlank()) {
                throw new IllegalStateException("requestId is required");
            }
            if (capabilityName == null || capabilityName.isBlank()) {
                throw new IllegalStateException("capabilityName is required");
            }
        }
    }
}