package com.shreeai.os.platform.gateway;

import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Gateway-level response wrapper.
 *
 * <p>Carries the runtime {@link ExecutionSession} back to the SDK layer
 * so that {@link ShreeClient} can construct the final {@link SDKResponse}.</p>
 */
public final class GatewayResponse {

    private final ExecutionSession executionSession;
    private final String gatewayRequestId;
    private final Instant gatewayTimestamp;

    private GatewayResponse(Builder builder) {
        this.executionSession = Objects.requireNonNull(builder.executionSession, "executionSession must not be null");
        this.gatewayRequestId = builder.gatewayRequestId;
        this.gatewayTimestamp = builder.gatewayTimestamp != null
                ? builder.gatewayTimestamp
                : Instant.now();
    }

    public ExecutionSession executionSession() { return executionSession; }
    public String gatewayRequestId() { return gatewayRequestId; }
    public Instant gatewayTimestamp() { return gatewayTimestamp; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private ExecutionSession executionSession;
        private String gatewayRequestId;
        private Instant gatewayTimestamp;

        private Builder() {}

        public Builder executionSession(ExecutionSession executionSession) {
            this.executionSession = executionSession;
            return this;
        }

        public Builder gatewayRequestId(String gatewayRequestId) {
            this.gatewayRequestId = gatewayRequestId;
            return this;
        }

        public Builder gatewayTimestamp(Instant gatewayTimestamp) {
            this.gatewayTimestamp = gatewayTimestamp;
            return this;
        }

        public GatewayResponse build() {
            return new GatewayResponse(this);
        }
    }
}
