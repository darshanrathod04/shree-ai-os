package com.shreeai.os.platform.gateway;

import com.shreeai.os.platform.sdk.SDKRequest;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Gateway-level request wrapper.
 *
 * <p>Encapsulates the SDK request together with gateway-normalized
 * metadata such as the gateway-assigned request identifier and
 * timestamps.</p>
 */
public final class GatewayRequest {

    private final SDKRequest sdkRequest;
    private final String gatewayRequestId;
    private final long gatewayTimestamp;
    private final Map<String, Object> normalizedMetadata;

    private GatewayRequest(Builder builder) {
        this.sdkRequest = Objects.requireNonNull(builder.sdkRequest, "sdkRequest must not be null");
        this.gatewayRequestId = builder.gatewayRequestId != null
                ? builder.gatewayRequestId
                : UUID.randomUUID().toString();
        this.gatewayTimestamp = builder.gatewayTimestamp != 0
                ? builder.gatewayTimestamp
                : System.currentTimeMillis();
        this.normalizedMetadata = builder.normalizedMetadata != null
                ? Map.copyOf(builder.normalizedMetadata)
                : Map.of();
    }

    public SDKRequest sdkRequest() { return sdkRequest; }
    public String gatewayRequestId() { return gatewayRequestId; }
    public long gatewayTimestamp() { return gatewayTimestamp; }
    public Map<String, Object> normalizedMetadata() { return normalizedMetadata; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private SDKRequest sdkRequest;
        private String gatewayRequestId;
        private long gatewayTimestamp;
        private Map<String, Object> normalizedMetadata;

        private Builder() {}

        public Builder sdkRequest(SDKRequest sdkRequest) {
            this.sdkRequest = sdkRequest;
            return this;
        }

        public Builder gatewayRequestId(String gatewayRequestId) {
            this.gatewayRequestId = gatewayRequestId;
            return this;
        }

        public Builder gatewayTimestamp(long gatewayTimestamp) {
            this.gatewayTimestamp = gatewayTimestamp;
            return this;
        }

        public Builder normalizedMetadata(Map<String, Object> normalizedMetadata) {
            this.normalizedMetadata = normalizedMetadata;
            return this;
        }

        public GatewayRequest build() {
            return new GatewayRequest(this);
        }
    }
}
