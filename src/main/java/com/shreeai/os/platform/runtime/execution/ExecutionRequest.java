package com.shreeai.os.platform.runtime.execution;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * <b>ExecutionRequest</b>
 *
 * <p>Represents a request for execution within the Runtime.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates all data required to initiate an execution.</li>
 *   <li>Carries a unique identifier for tracking and correlation.</li>
 *   <li>Is immutable after construction.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Invariant:</b> Every ExecutionRequest MUST have a non-null, non-empty requestId.</p>
 */
public final class ExecutionRequest {

    private final String requestId;
    private final String requestType;
    private final String payload;
    private final String context;
    private final Map<String, Object> metadata;
    private final Instant createdAt;

    private ExecutionRequest(Builder builder) {
        this.requestId = builder.requestId;
        this.requestType = builder.requestType;
        this.payload = builder.payload;
        this.context = builder.context;
        this.metadata = builder.metadata != null
                ? Collections.unmodifiableMap(new HashMap<>(builder.metadata))
                : Collections.emptyMap();
        this.createdAt = builder.createdAt;
    }

    /**
     * Returns the unique identifier for this request.
     *
     * @return the request ID
     */
    public String requestId() {
        return requestId;
    }

    /**
     * Returns the type of this execution request.
     *
     * @return the request type
     */
    public String requestType() {
        return requestType;
    }

    /**
     * Returns the payload data for this request.
     *
     * @return the payload
     */
    public String payload() {
        return payload;
    }

    /**
     * Returns the context for this request.
     *
     * @return the context
     */
    public String context() {
        return context;
    }

    /**
     * Returns the metadata for this request.
     *
     * @return the metadata
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Returns the timestamp when this request was created.
     *
     * @return the creation timestamp
     */
    public Instant createdAt() {
        return createdAt;
    }

    /**
     * Creates a new builder for ExecutionRequest.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link ExecutionRequest}.
     */
    public static final class Builder {

        private String requestId;
        private String requestType;
        private String payload;
        private String context = "";
        private Map<String, Object> metadata = new HashMap<>();
        private Instant createdAt;

        private Builder() {
        }

        /**
         * Sets the request ID. If not set, a UUID will be generated.
         *
         * @param requestId the request ID
         * @return this builder
         */
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        /**
         * Sets the request type.
         *
         * @param requestType the request type
         * @return this builder
         */
        public Builder requestType(String requestType) {
            this.requestType = requestType;
            return this;
        }

        /**
         * Sets the payload.
         *
         * @param payload the payload data
         * @return this builder
         */
        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        /**
         * Sets the context.
         *
         * @param context the context
         * @return this builder
         */
        public Builder context(String context) {
            this.context = context != null ? context : "";
            return this;
        }

        /**
         * Sets the metadata.
         *
         * @param metadata the metadata
         * @return this builder
         */
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
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
         * Builds a new ExecutionRequest.
         *
         * @return a new request instance
         */
        public ExecutionRequest build() {
            if (requestId == null || requestId.isBlank()) {
                requestId = UUID.randomUUID().toString();
            }
            if (createdAt == null) {
                createdAt = Instant.now();
            }
            return new ExecutionRequest(this);
        }
    }
}