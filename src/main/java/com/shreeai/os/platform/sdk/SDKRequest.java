package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.sdk.exceptions.ValidationException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>SDKRequest</b>
 *
 * <p>Immutable request model for the Shree AI OS SDK.</p>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0.0-V1</p>
 */
public final class SDKRequest {

    private final String message;
    private final String context;
    private final Map<String, Object> metadata;
    private final String sessionId;
    private final String userId;

    private SDKRequest(Builder builder) {
        this.message = builder.message;
        this.context = builder.context;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
        this.sessionId = builder.sessionId;
        this.userId = builder.userId;
    }

    public String message() { return message; }
    public String context() { return context; }
    public Map<String, Object> metadata() { return metadata; }
    public String sessionId() { return sessionId; }
    public String userId() { return userId; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String message;
        private String context = "";
        private Map<String, Object> metadata = new HashMap<>();
        private String sessionId;
        private String userId;

        private Builder() {}

        public Builder message(String message) {
            this.message = Objects.requireNonNull(message, "message must not be null");
            return this;
        }

        public Builder context(String context) {
            this.context = Objects.requireNonNull(context, "context must not be null");
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = Objects.requireNonNull(metadata, "metadata must not be null");
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public SDKRequest build() {
            if (message == null || message.isBlank()) {
                throw new ValidationException("message must not be null or blank");
            }
            return new SDKRequest(this);
        }
    }
}