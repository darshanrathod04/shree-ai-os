package com.shreeai.os.platform.legacy.execution;

import com.shreeai.os.platform.legacy.context.ConversationSession;
import com.shreeai.os.platform.legacy.production.ResolvedContext;

import java.time.Instant;
import java.util.Objects;
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
 * <p><b>Equality contract:</b> timestamp is intentionally excluded from
 * logical equality because it represents request creation telemetry rather
 * than request identity.</p>
 *
 * @author Shree AI OS Team
 * @version 2.0
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

    public static Builder builder() {
        return new Builder();
    }

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
                ", session=" +
                (session != null ? session.getSessionId() : "null") +
                ", resolvedContext=" + resolvedContext +
                ", metadata=" + metadata +
                ", timestamp=" + timestamp +
                '}';
    }

    /**
     * Logical equality deliberately excludes timestamp.
     *
     * <p>Two requests created at different instants can still represent
     * the exact same execution contract. Timestamp is telemetry and should
     * not alter identity semantics.</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ExecutionRequest that)) {
            return false;
        }

        return Objects.equals(requestId, that.requestId)
                && Objects.equals(decisionId, that.decisionId)
                && Objects.equals(capabilityName, that.capabilityName)
                && Objects.equals(intent, that.intent)
                && Objects.equals(userInput, that.userInput)
                && Objects.equals(session, that.session)
                && Objects.equals(resolvedContext, that.resolvedContext)
                && Objects.equals(metadata, that.metadata);
    }

    /**
     * Hash contract mirrors equals().
     *
     * <p>timestamp is intentionally excluded.</p>
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                requestId,
                decisionId,
                capabilityName,
                intent,
                userInput,
                session,
                resolvedContext,
                metadata
        );
    }

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

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder decisionId(String decisionId) {
            this.decisionId = decisionId;
            return this;
        }

        public Builder capabilityName(String capabilityName) {
            this.capabilityName = capabilityName;
            return this;
        }

        public Builder intent(String intent) {
            this.intent = intent;
            return this;
        }

        public Builder userInput(String userInput) {
            this.userInput = userInput;
            return this;
        }

        public Builder session(ConversationSession session) {
            this.session = session;
            return this;
        }

        public Builder resolvedContext(ResolvedContext resolvedContext) {
            this.resolvedContext = resolvedContext;
            return this;
        }

        public Builder metadata(ExecutionMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ExecutionRequest build() {
            validateRequiredFields();

            return new ExecutionRequest(
                    requestId,
                    decisionId,
                    capabilityName,
                    intent,
                    userInput,
                    session,
                    resolvedContext,
                    metadata,
                    timestamp
            );
        }

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