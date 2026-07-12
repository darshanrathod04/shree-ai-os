package platform.runtime.execution;

import java.time.Instant;
import java.util.Optional;

/**
 * <b>ExecutionResult</b>
 *
 * <p>Represents the result of an execution within the Runtime.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the outcome of an execution, including success or failure.</li>
 *   <li>Provides access to the output payload and error information.</li>
 *   <li>Is immutable after construction.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 */
public final class ExecutionResult {

    private final String requestId;
    private final boolean success;
    private final String output;
    private final String errorMessage;
    private final Instant completedAt;

    private ExecutionResult(Builder builder) {
        this.requestId = builder.requestId;
        this.success = builder.success;
        this.output = builder.output;
        this.errorMessage = builder.errorMessage;
        this.completedAt = builder.completedAt;
    }

    /**
     * Returns the request ID associated with this result.
     *
     * @return the request ID
     */
    public String requestId() {
        return requestId;
    }

    /**
     * Returns whether the execution completed successfully.
     *
     * @return true if successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the output payload of the execution.
     *
     * @return the output, or empty if failed
     */
    public Optional<String> output() {
        return Optional.ofNullable(output);
    }

    /**
     * Returns the error message if the execution failed.
     *
     * @return the error message, or empty if successful
     */
    public Optional<String> errorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    /**
     * Returns the timestamp when the execution completed.
     *
     * @return the completion timestamp
     */
    public Instant completedAt() {
        return completedAt;
    }

    /**
     * Creates a successful ExecutionResult.
     *
     * @param requestId the request ID
     * @param output    the output payload
     * @return a successful result
     */
    public static ExecutionResult success(String requestId, String output) {
        return builder()
                .requestId(requestId)
                .success(true)
                .output(output)
                .build();
    }

    /**
     * Creates a failed ExecutionResult.
     *
     * @param requestId    the request ID
     * @param errorMessage the error message
     * @return a failed result
     */
    public static ExecutionResult failure(String requestId, String errorMessage) {
        return builder()
                .requestId(requestId)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Creates a new builder for ExecutionResult.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link ExecutionResult}.
     */
    public static final class Builder {

        private String requestId;
        private boolean success;
        private String output;
        private String errorMessage;
        private Instant completedAt;

        private Builder() {
        }

        /**
         * Sets the request ID.
         *
         * @param requestId the request ID
         * @return this builder
         */
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        /**
         * Sets the success flag.
         *
         * @param success whether execution was successful
         * @return this builder
         */
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        /**
         * Sets the output payload.
         *
         * @param output the output data
         * @return this builder
         */
        public Builder output(String output) {
            this.output = output;
            return this;
        }

        /**
         * Sets the error message.
         *
         * @param errorMessage the error description
         * @return this builder
         */
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        /**
         * Sets the completion timestamp. If not set, the current time will be used.
         *
         * @param completedAt the completion timestamp
         * @return this builder
         */
        public Builder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        /**
         * Builds a new ExecutionResult.
         *
         * @return a new result instance
         */
        public ExecutionResult build() {
            if (completedAt == null) {
                completedAt = Instant.now();
            }
            return new ExecutionResult(this);
        }
    }
}