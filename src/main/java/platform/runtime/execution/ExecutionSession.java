package platform.runtime.execution;

import java.time.Instant;
import java.util.UUID;

/**
 * <b>ExecutionSession</b>
 *
 * <p>Represents a tracked execution session within the Runtime.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Tracks the lifecycle of a single execution from request to result.</li>
 *   <li>Provides session-level context and state management.</li>
 *   <li>Enables Runtime to manage concurrent executions.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Invariant:</b> Every ExecutionSession MUST have a non-null, non-empty sessionId.</p>
 */
public final class ExecutionSession {

    private final String sessionId;
    private final String requestId;
    private final SessionStatus status;
    private final Instant createdAt;

    /**
     * Possible status values for an execution session.
     */
    public enum SessionStatus {
        PENDING,
        ACTIVE,
        COMPLETED,
        FAILED,
        TIMEOUT,
        ABORTED
    }

    private ExecutionSession(Builder builder) {
        this.sessionId = builder.sessionId;
        this.requestId = builder.requestId;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
    }

    /**
     * Returns the unique session identifier.
     *
     * @return the session ID
     */
    public String sessionId() {
        return sessionId;
    }

    /**
     * Returns the request ID associated with this session.
     *
     * @return the request ID
     */
    public String requestId() {
        return requestId;
    }

    /**
     * Returns the current status of this session.
     *
     * @return the session status
     */
    public SessionStatus status() {
        return status;
    }

    /**
     * Returns the timestamp when this session was created.
     *
     * @return the creation timestamp
     */
    public Instant createdAt() {
        return createdAt;
    }

    /**
     * Creates a new builder for ExecutionSession.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link ExecutionSession}.
     */
    public static final class Builder {

        private String sessionId;
        private String requestId;
        private SessionStatus status = SessionStatus.PENDING;
        private Instant createdAt;

        private Builder() {
        }

        /**
         * Sets the session ID. If not set, a UUID will be generated.
         *
         * @param sessionId the session ID
         * @return this builder
         */
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        /**
         * Sets the associated request ID.
         *
         * @param requestId the request ID
         * @return this builder
         */
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        /**
         * Sets the session status.
         *
         * @param status the session status
         * @return this builder
         */
        public Builder status(SessionStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Sets the creation timestamp. If not set, the current time will be used.
         *
         * @param createdAt the creation timestamp
         * @return this builder
         */
        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * Builds a new ExecutionSession.
         *
         * @return a new session instance
         */
        public ExecutionSession build() {
            if (sessionId == null || sessionId.isBlank()) {
                sessionId = UUID.randomUUID().toString();
            }
            if (createdAt == null) {
                createdAt = Instant.now();
            }
            return new ExecutionSession(this);
        }
    }
}