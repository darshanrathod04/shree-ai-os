package com.shreeai.os.platform.execution;

import com.shreeai.os.platform.context.ConversationSession;
import com.shreeai.os.platform.production.ResolvedContext;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable execution request model.
 *
 * <p>This class represents a request to execute a capability.
 * It contains all necessary information for the Runtime Layer to execute
 * a capability including the decision, context, and metadata.</p>
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
public final class ExecutionRequest {

    private final String requestId;
    private final String decisionId;
    private final String capabilityName;
    private final String intent;
    private final String userInput;
    private final ConversationSession session;
    private final ResolvedContext resolvedContext;
    private final ExecutionMetadata metadata;
    private final Instant timestamp;

    /**
     * Create ExecutionRequest with builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Private constructor - use builder for construction.
     */
    private ExecutionRequest(
            String requestId,
            String decisionId,
            String capabilityName,
            String intent,
            String userInput,
            ConversationSession session,
            ResolvedContext resolvedContext,
            ExecutionMetadata metadata,
            Instant timestamp
    ) {
        this.requestId = requestId;
        this.decisionId = decisionId;
        this.capabilityName = capabilityName;
        this.intent = intent;
        this.userInput = userInput;
        this.session = session;
        this.resolvedContext = resolvedContext;
        this.metadata = metadata;
        this.timestamp = timestamp;
    }

    // Getters
    public String getRequestId() {
        return requestId;
    }

    public String getDecisionId() {
        return decisionId;
    }

    public String getCapabilityName() {
        return capabilityName;
    }

    public String getIntent() {
        return intent;
    }

    public String getUserInput() {
        return userInput;
    }

    public ConversationSession getSession() {
        return session;
    }

    public ResolvedContext getResolvedContext() {
        return resolvedContext;
    }

    public ExecutionMetadata getMetadata() {
        return metadata;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "ExecutionRequest{" +
                "requestId='" + requestId + '\'' +
                ", decisionId='" + decisionId + '\'' +
                ", capabilityName='" + capabilityName + '\'' +
                ", intent='" + intent + '\'' +
                ", userInput='" + userInput + '\'' +
                ", session=" + (session != null ? session.getSessionId() : "null") +
                ", resolvedContext=" + resolvedContext +
                ", metadata=" + metadata +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExecutionRequest that = (ExecutionRequest) o;

        if (!requestId.equals(that.requestId)) return false;
        if (!decisionId.equals(that.decisionId)) return false;
        if (!capabilityName.equals(that.capabilityName)) return false;
        if (!intent.equals(that.intent)) return false;
        if (!userInput.equals(that.userInput)) return false;
        if (session != null ? !session.equals(that.session) : that.session != null) return false;
        if (resolvedContext != null ? !resolvedContext.equals(that.resolvedContext) : that.resolvedContext != null)
            return false;
        if (!metadata.equals(that.metadata)) return false;
        return timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        int result = requestId.hashCode();
        result = 31 * result + decisionId.hashCode();
        result = 31 * result + capabilityName.hashCode();
        result = 31 * result + intent.hashCode();
        result = 31 * result + userInput.hashCode();
        result = 31 * result + (session != null ? session.hashCode() : 0);
        result = 31 * result + (resolvedContext != null ? resolvedContext.hashCode() : 0);
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        result = 31 * result + timestamp.hashCode();
        return result;
    }

    /**
     * Builder for ExecutionRequest.
     */
    public static class Builder {
        private String requestId = UUID.randomUUID().toString();
        private String decisionId;
        private String capabilityName;
        private String intent;
        private String userInput;
        private ConversationSession session;
        private ResolvedContext resolvedContext;
        private ExecutionMetadata metadata;
        private Instant timestamp = Instant.now();

        /**
         * Set the request ID (defaults to new UUID if not set).
         *
         * @param requestId the request ID
         * @return this builder
         */
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        /**
         * Set the decision ID (required).
         *
         * @param decisionId the decision ID
         * @return this builder
         */
        public Builder decisionId(String decisionId) {
            this.decisionId = decisionId;
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
         * Set the intent (required).
         *
         * @param intent the intent
         * @return this builder
         */
        public Builder intent(String intent) {
            this.intent = intent;
            return this;
        }

        /**
         * Set the user input (required).
         *
         * @param userInput the user input
         * @return this builder
         */
        public Builder userInput(String userInput) {
            this.userInput = userInput;
            return this;
        }

        /**
         * Set the conversation session.
         *
         * @param session the conversation session
         * @return this builder
         */
        public Builder session(ConversationSession session) {
            this.session = session;
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
         * Build the ExecutionRequest instance.
         *
         * @return a new ExecutionRequest instance
         * @throws IllegalStateException if required fields are missing
         */
        public ExecutionRequest build() {
            validateRequiredFields();
            return new ExecutionRequest(
                    requestId, decisionId, capabilityName, intent, userInput,
                    session, resolvedContext, metadata, timestamp
            );
        }

        /**
         * Validate that all required fields are present.
         *
         * @throws IllegalStateException if any required field is missing
         */
        private void validateRequiredFields() {
            if (decisionId == null || decisionId.isBlank()) {
                throw new IllegalStateException("decisionId is required");
            }
            if (capabilityName == null || capabilityName.isBlank()) {
                throw new IllegalStateException("capabilityName is required");
            }
            if (intent == null || intent.isBlank()) {
                throw new IllegalStateException("intent is required");
            }
            if (userInput == null) {
                throw new IllegalStateException("userInput is required");
            }
        }
    }
}