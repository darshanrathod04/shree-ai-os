package com.shreeai.os.platform.runtime.execution;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * <b>RichExecutionResult</b>
 *
 * <p>Immutable, structured result of a capability-driven execution dispatched
 * through the {@link ExecutionDispatcher}. Carries execution identity,
 * capability, lifecycle status, timing metrics, confidence, and output.</p>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Version:</b> 2.1</p>
 *
 * @since 2.1
 */
public final class RichExecutionResult {

    private final String executionId;
    private final ExecutionCapability capability;
    private final ExecutionStatus status;
    private final Instant startedAt;
    private final Instant completedAt;
    private final double confidence;
    private final String output;
    private final Map<String, Object> metadata;

    private RichExecutionResult(Builder builder) {
        this.executionId = builder.executionId;
        this.capability = builder.capability;
        this.status = builder.status;
        this.startedAt = builder.startedAt;
        this.completedAt = builder.completedAt;
        this.confidence = builder.confidence;
        this.output = builder.output;
        this.metadata = Map.copyOf(builder.metadata);
    }

    /** @return the unique execution identifier (never null) */
    public String executionId() {
        return executionId;
    }

    /** @return the capability that was dispatched (never null) */
    public ExecutionCapability capability() {
        return capability;
    }

    /** @return the terminal status of the execution (never null) */
    public ExecutionStatus status() {
        return status;
    }

    /** @return when the execution started (never null) */
    public Instant startedAt() {
        return startedAt;
    }

    /** @return when the execution completed (never null) */
    public Instant completedAt() {
        return completedAt;
    }

    /** @return wall-clock duration in milliseconds (never negative) */
    public long durationMs() {
        return Duration.between(startedAt, completedAt).toMillis();
    }

    /** @return confidence in [0.0, 1.0] */
    public double confidence() {
        return confidence;
    }

    /** @return the output payload (never null; may be empty) */
    public String output() {
        return output == null ? "" : output;
    }

    /** @return unmodifiable metadata (never null) */
    public Map<String, Object> metadata() {
        return metadata;
    }


    /** @return true when the status is SUCCESS */
    public boolean isSuccess() {
        return status.isSuccess();
    }

    /**
     * Converts this rich result to a legacy {@link ExecutionResult} for
     * backward compatibility with the existing Runtime contract.
     *
     * @return an ExecutionResult carrying equivalent information
     */
    public ExecutionResult toExecutionResult() {
        return ExecutionResult.builder()
                .requestId(executionId)
                .success(status.isSuccess())
                .output(output)
                .errorMessage(status.isSuccess() ? null : output)
                .completedAt(completedAt)
                .structuredPayload(metadata)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RichExecutionResult that)) {
            return false;
        }
        return Double.compare(that.confidence, confidence) == 0
                && executionId.equals(that.executionId)
                && capability == that.capability
                && status == that.status
                && startedAt.equals(that.startedAt)
                && completedAt.equals(that.completedAt)
                && Objects.equals(output, that.output)
                && metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionId, capability, status, startedAt, completedAt,
                confidence, output, metadata);
    }

    /* ==========================================================
       Factory Methods
       ========================================================== */

    /**
     * Creates a new builder for RichExecutionResult.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a successful rich execution result.
     *
     * @param capability the dispatched capability
     * @param output     the execution output
     * @param confidence the execution confidence
     * @return a successful rich result
     */
    public static RichExecutionResult success(
            ExecutionCapability capability,
            String output,
            double confidence) {
        Instant now = Instant.now();
        return new Builder()
                .executionId(UUID.randomUUID().toString())
                .capability(capability)
                .status(ExecutionStatus.SUCCESS)
                .startedAt(now)
                .completedAt(now)
                .confidence(confidence)
                .output(output)
                .metadata(Map.of())
                .build();
    }

    /**
     * Creates a failed rich execution result.
     *
     * @param capability the dispatched capability
     * @param error      the error message
     * @return a failed rich result
     */
    public static RichExecutionResult failure(
            ExecutionCapability capability,
            String error) {
        Instant now = Instant.now();
        return new Builder()
                .executionId(UUID.randomUUID().toString())
                .capability(capability)
                .status(ExecutionStatus.FAILED)
                .startedAt(now)
                .completedAt(now)
                .confidence(0.0)
                .output(error)
                .metadata(Map.of())
                .build();
    }

    /**
     * Creates a denied rich execution result (execution stopped).
     *
     * @param capability the dispatched capability
     * @param reason     the denial reason
     * @return a denied rich result
     */
    public static RichExecutionResult denied(
            ExecutionCapability capability,
            String reason) {
        Instant now = Instant.now();
        return new Builder()
                .executionId(UUID.randomUUID().toString())
                .capability(capability)
                .status(ExecutionStatus.DENIED)
                .startedAt(now)
                .completedAt(now)
                .confidence(0.0)
                .output(reason)
                .metadata(Map.of("denied", true))
                .build();
    }

    /**
     * Creates a pending-approval rich execution result.
     *
     * @param capability the dispatched capability
     * @param reason     the reason approval is required
     * @return a pending-approval rich result
     */
    public static RichExecutionResult pendingApproval(
            ExecutionCapability capability,
            String reason) {
        Instant now = Instant.now();
        return new Builder()
                .executionId(UUID.randomUUID().toString())
                .capability(capability)
                .status(ExecutionStatus.PENDING_APPROVAL)
                .startedAt(now)
                .completedAt(now)
                .confidence(0.0)
                .output(reason)
                .metadata(Map.of("pendingApproval", true))
                .build();
    }

    @Override
    public String toString() {
        return "RichExecutionResult{"
                + "executionId='" + executionId + '\''
                + ", capability=" + capability
                + ", status=" + status
                + ", startedAt=" + startedAt
                + ", completedAt=" + completedAt
                + ", durationMs=" + durationMs()
                + ", confidence=" + confidence
                + ", output='" + output + '\''
                + '}';
    }

    /* ==========================================================
       Builder
       ========================================================== */

    /**
     * Fluent builder for {@link RichExecutionResult}.
     */
    public static final class Builder {

        private String executionId = UUID.randomUUID().toString();
        private ExecutionCapability capability;
        private ExecutionStatus status = ExecutionStatus.SUCCESS;
        private Instant startedAt = Instant.now();
        private Instant completedAt = Instant.now();
        private double confidence = 0.0;
        private String output = "";
        private Map<String, Object> metadata = Map.of();

        private Builder() {
        }

        public Builder executionId(String executionId) {
            this.executionId = Objects.requireNonNull(
                    executionId, "executionId must not be null");
            return this;
        }

        public Builder capability(ExecutionCapability capability) {
            this.capability = Objects.requireNonNull(
                    capability, "capability must not be null");
            return this;
        }

        public Builder status(ExecutionStatus status) {
            this.status = Objects.requireNonNull(
                    status, "status must not be null");
            return this;
        }

        public Builder startedAt(Instant startedAt) {
            this.startedAt = Objects.requireNonNull(
                    startedAt, "startedAt must not be null");
            return this;
        }

        public Builder completedAt(Instant completedAt) {
            this.completedAt = Objects.requireNonNull(
                    completedAt, "completedAt must not be null");
            return this;
        }

        public Builder confidence(double confidence) {
            if (confidence < 0.0 || confidence > 1.0) {
                throw new IllegalArgumentException(
                        "confidence must be in [0.0, 1.0]");
            }
            this.confidence = confidence;
            return this;
        }

        public Builder output(String output) {
            this.output = output == null ? "" : output;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata == null
                    ? Map.of()
                    : new java.util.HashMap<>(metadata);
            return this;
        }

        public RichExecutionResult build() {
            if (capability == null) {
                throw new IllegalArgumentException(
                        "capability must not be null");
            }
            return new RichExecutionResult(this);
        }
    }
}

