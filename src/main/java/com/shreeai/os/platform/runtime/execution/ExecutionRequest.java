package com.shreeai.os.platform.runtime.execution;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Canonical Runtime Execution Request.
 *
 * This is the ONLY request contract used inside the Runtime pipeline.
 * Legacy getters are preserved for backward compatibility.
 */
public final class ExecutionRequest {

    private final String requestId;
    private final String requestType;
    private final String payload;
    private final String context;
    private final Map<String, Object> metadata;

    private ExecutionRequest(Builder builder) {
        this.requestId = builder.requestId;
        this.requestType = builder.requestType;
        this.payload = builder.payload;
        this.context = builder.context;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
    }

    public static Builder builder() {
        return new Builder();
    }

    /* ==========================================================
       Canonical Runtime API
       ========================================================== */

    public String requestId() {
        return requestId;
    }

    public String requestType() {
        return requestType;
    }

    public String payload() {
        return payload;
    }

    public String context() {
        return context;
    }

    public Map<String, Object> metadata() {
        return metadata;
    }

    /* ==========================================================
       Legacy Compatibility API
       These remove 18+ compile errors without breaking old stages.
       ========================================================== */

    public String getRequestId() {
        return requestId;
    }

    public String getUserInput() {
        return payload;
    }

    public String getPayload() {
        return payload;
    }

    public String getContext() {
        return context;
    }

    public String getRequestType() {
        return requestType;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public String getSession() {
        Object value = metadata.get("sessionId");
        return value == null ? null : value.toString();
    }

    /* ==========================================================
       Builder
       ========================================================== */

    public static final class Builder {

        private String requestId = UUID.randomUUID().toString();
        private String requestType = "CHAT";
        private String payload = "";
        private String context = "";
        private Map<String, Object> metadata = new HashMap<>();

        public Builder requestId(String requestId) {
            this.requestId = Objects.requireNonNull(requestId);
            return this;
        }

        public Builder requestType(String requestType) {
            this.requestType = Objects.requireNonNull(requestType);
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload == null ? "" : payload;
            return this;
        }

        public Builder context(String context) {
            this.context = context == null ? "" : context;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            if (metadata != null) {
                this.metadata = new HashMap<>(metadata);
            }
            return this;
        }

        public Builder addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public ExecutionRequest build() {
            return new ExecutionRequest(this);
        }
    }
}