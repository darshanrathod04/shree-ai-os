package com.shreeai.os.platform.kernels.execution.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * <b>ExecutionRequest</b>
 *
 * <p>Represents a request to execute work.
 * This immutable value object encapsulates execution intent only.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates execution request parameters.</li>
 *   <li>Provides immutable execution context.</li>
 *   <li>Defines execution options and constraints.</li>
 *   <li>Contains no execution behavior.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable â€” all fields are final.</li>
 *   <li>Constructor validation â€” rejects null arguments.</li>
 *   <li>Defensive copying â€” protects mutable collections.</li>
 *   <li>Value-based equality â€” implements equals, hashCode, toString.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Execution Kernel â€” Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-EXEC-102, EIO-ARCH-001</p>
 *
 * @param executionId    the execution identifier (must not be {@code null})
 * @param actionId       the action identifier to execute (must not be {@code null})
 * @param context        the execution context (must not be {@code null})
 * @param options        the execution options (must not be {@code null})
 * @param parameters     additional execution parameters (must not be {@code null})
 *
 * @since 1.0
 */
public final class ExecutionRequest {

    private final ExecutionId executionId;
    private final String actionId;
    private final ExecutionContext context;
    private final ExecutionOptions options;
    private final Map<String, Object> parameters;

    /**
     * Constructs an {@code ExecutionRequest} with the specified parameters.
     *
     * @param executionId the execution identifier (must not be {@code null})
     * @param actionId    the action identifier to execute (must not be {@code null} or empty)
     * @param context     the execution context (must not be {@code null})
     * @param options     the execution options (must not be {@code null})
     * @param parameters  additional execution parameters (must not be {@code null})
     * @throws IllegalArgumentException if any parameter is {@code null} or empty
     */
    public ExecutionRequest(
            ExecutionId executionId,
            String actionId,
            ExecutionContext context,
            ExecutionOptions options,
            Map<String, Object> parameters) {
        if (executionId == null) {
            throw new IllegalArgumentException("ExecutionRequest executionId must not be null");
        }
        if (actionId == null || actionId.trim().isEmpty()) {
            throw new IllegalArgumentException("ExecutionRequest actionId must not be null or empty");
        }
        if (context == null) {
            throw new IllegalArgumentException("ExecutionRequest context must not be null");
        }
        if (options == null) {
            throw new IllegalArgumentException("ExecutionRequest options must not be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("ExecutionRequest parameters must not be null");
        }

        this.executionId = executionId;
        this.actionId = actionId;
        this.context = context;
        this.options = options;
        this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
    }

    /**
     * Returns the execution identifier.
     *
     * @return the execution identifier
     */
    public ExecutionId executionId() {
        return executionId;
    }

    /**
     * Returns the action identifier to execute.
     *
     * @return the action identifier
     */
    public String actionId() {
        return actionId;
    }

    /**
     * Returns the execution context.
     *
     * @return the execution context
     */
    public ExecutionContext context() {
        return context;
    }

    /**
     * Returns the execution options.
     *
     * @return the execution options
     */
    public ExecutionOptions options() {
        return options;
    }

    /**
     * Returns an unmodifiable view of the execution parameters.
     *
     * <p>The returned map is unmodifiable and reflects the parameters at the
     * time of this call.</p>
     *
     * @return an unmodifiable map of parameters
     */
    public Map<String, Object> parameters() {
        return parameters;
    }

    /**
     * Returns the request ID (the string value of the execution identifier).
     * This is the primary identity of the request, equivalent to executionId.value().
     */
    public String getRequestId() {
        return executionId != null ? executionId.value() : "";
    }

    /** Returns the user input stored in parameters, or empty string if not set. */
    public String getUserInput() {
        Object v = parameters.get("userInput");
        return v != null ? v.toString() : "";
    }

    /** Returns the decision ID stored in parameters, or empty string if not set. */
    public String getDecisionId() {
        Object v = parameters.get("decisionId");
        return v != null ? v.toString() : "";
    }

    /** Returns the capability name stored in parameters, or empty string if not set. */
    public String getCapabilityName() {
        Object v = parameters.get("capabilityName");
        return v != null ? v.toString() : "";
    }

    /** Returns the intent stored in parameters, or empty string if not set. */
    public String getIntent() {
        Object v = parameters.get("intent");
        return v != null ? v.toString() : "";
    }

    /** Returns the session ID stored in parameters, or empty string if not set. */
    public String getSession() {
        Object v = parameters.get("session");
        return v != null ? v.toString() : "";
    }

    /** Returns metadata parameters as a Map. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMetadata() {
        return parameters;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two {@code ExecutionRequest} instances are equal if they have the same
     * execution identifier, action identifier, context, options, and parameters.</p>
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the {@code obj} argument
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ExecutionRequest that = (ExecutionRequest) obj;
        return Objects.equals(executionId, that.executionId) &&
                Objects.equals(actionId, that.actionId) &&
                Objects.equals(context, that.context) &&
                Objects.equals(options, that.options) &&
                Objects.equals(parameters, that.parameters);
    }

    /**
     * Returns a hash code value for this {@code ExecutionRequest}.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(executionId, actionId, context, options, parameters);
    }

    /**
     * Returns a string representation of this {@code ExecutionRequest}.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ExecutionRequest{" +
                "executionId=" + executionId +
                ", actionId='" + actionId + "'" +
                "context=" + context +
                ", options=" + options +
                ", parameters=" + parameters +
                "}";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ExecutionId executionId;
        private String actionId;
        private ExecutionContext context;
        private ExecutionOptions options;
        private Map<String, Object> parameters = new HashMap<>();

        public Builder executionId(ExecutionId executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder requestId(String requestId) {
            this.executionId = requestId != null ? new ExecutionId(requestId) : null;
            return this;
        }

        public Builder actionId(String actionId) {
            this.actionId = actionId;
            return this;
        }

        public Builder requestType(String requestType) {
            this.actionId = requestType;
            return this;
        }

        public Builder intent(String intent) {
            this.parameters.put("intent", intent);
            return this;
        }

        public Builder userInput(String userInput) {
            this.parameters.put("userInput", userInput);
            return this;
        }

        public Builder decisionId(String decisionId) {
            this.parameters.put("decisionId", decisionId);
            return this;
        }

        public Builder capabilityName(String capabilityName) {
            this.parameters.put("capabilityName", capabilityName);
            return this;
        }

        public Builder context(ExecutionContext context) {
            this.context = context;
            return this;
        }

        public Builder options(ExecutionOptions options) {
            this.options = options;
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
            return this;
        }

        public Builder addParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Builder addMetadata(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Builder payload(String payload) {
            this.parameters.put("payload", payload);
            return this;
        }

        public ExecutionRequest build() {
            // Backward-compatibility: if no context is supplied, construct a valid
            // default ExecutionContext so existing callers (SDK, Runtime, pipeline
            // conversions, tests) that do not pass a context continue to work.
            // The domain model (ExecutionContext constructor) still validates
            // non-null executionId/planId/objectiveId, so we must always satisfy
            // those invariants. Production callers that need a real context
            // continue to set it via .context(...).
            ExecutionContext effectiveContext = context;
            if (effectiveContext == null) {
                ExecutionId contextExecutionId = executionId != null
                        ? executionId
                        : new ExecutionId("exec-" + UUID.randomUUID());
                effectiveContext = new ExecutionContext(
                        contextExecutionId,
                        "default",
                        "default",
                        new HashMap<>(),
                        0
                );
            }
            return new ExecutionRequest(
                executionId,
                actionId,
                effectiveContext,
                options != null ? options : ExecutionOptions.defaults(),
                parameters
            );
        }
    }
}
