package com.shreeai.os.platform.gateway;

import com.shreeai.os.platform.sdk.SDKRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Carries gateway processing state from request validation through
 * forwarding to the runtime.
 *
 * <p>Instances are mutable during processing but the final state is
 * captured in {@link GatewayRequest} and {@link GatewayResponse}.</p>
 */
public final class GatewayContext {

    private final SDKRequest sdkRequest;
    private final String gatewayRequestId;
    private final Map<String, Object> metadata;
    private boolean validated;
    private boolean forwarded;
    private String normalizedSessionId;

    private GatewayContext(Builder builder) {
        this.sdkRequest = Objects.requireNonNull(builder.sdkRequest, "sdkRequest must not be null");
        this.gatewayRequestId = builder.gatewayRequestId != null
                ? builder.gatewayRequestId
                : java.util.UUID.randomUUID().toString();
        this.metadata = new HashMap<>();
        if (builder.metadata != null) {
            this.metadata.putAll(builder.metadata);
        }
        this.validated = false;
        this.forwarded = false;
        this.normalizedSessionId = builder.sessionId;
    }

    public SDKRequest sdkRequest() { return sdkRequest; }
    public String gatewayRequestId() { return gatewayRequestId; }
    public Map<String, Object> metadata() { return Map.copyOf(metadata); }
    public boolean isValidated() { return validated; }
    public boolean isForwarded() { return forwarded; }
    public String normalizedSessionId() { return normalizedSessionId; }

    void setValidated(boolean validated) { this.validated = validated; }
    void setForwarded() { this.forwarded = true; }
    void setNormalizedSessionId(String normalizedSessionId) { this.normalizedSessionId = normalizedSessionId; }
    void addMetadata(String key, Object value) { this.metadata.put(key, value); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private SDKRequest sdkRequest;
        private String gatewayRequestId;
        private String sessionId;
        private Map<String, Object> metadata;

        private Builder() {}

        public Builder sdkRequest(SDKRequest sdkRequest) {
            this.sdkRequest = sdkRequest;
            return this;
        }

        public Builder gatewayRequestId(String gatewayRequestId) {
            this.gatewayRequestId = gatewayRequestId;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public GatewayContext build() {
            return new GatewayContext(this);
        }
    }
}
