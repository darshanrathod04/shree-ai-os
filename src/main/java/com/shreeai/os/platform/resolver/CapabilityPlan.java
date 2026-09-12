package com.shreeai.os.platform.resolver;

import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult.IntentType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>CapabilityPlan</b>
 *
 * <p>Immutable output of the {@code CapabilityResolver} for a single request.
 * Declares the detected intent and the ordered set of capabilities the
 * request requires.</p>
 *
 * <p>The plan is a decision artifact only: nothing in a {@code CapabilityPlan}
 * executes capabilities, and the Runtime currently ignores it.</p>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final.</li>
 *   <li>Builder-only construction — no public constructors.</li>
 *   <li>Defensive copies — protects mutable collections.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Resolver</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class CapabilityPlan {

    private final String requestId;
    private final IntentType detectedIntent;
    private final List<CapabilityRequirement> requiredCapabilities;
    private final double confidence;
    private final Map<String, Object> metadata;

    private CapabilityPlan(Builder builder) {
        this.requestId = Objects.requireNonNull(builder.requestId, "requestId must not be null");
        this.detectedIntent = Objects.requireNonNull(builder.detectedIntent, "detectedIntent must not be null");
        this.requiredCapabilities = List.copyOf(builder.requiredCapabilities);
        this.confidence = builder.confidence;
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(builder.metadata));
    }

    /**
     * Returns the identifier of the request this plan was resolved for.
     *
     * @return the request ID
     */
    public String requestId() {
        return requestId;
    }

    /**
     * Returns the detected intent of the request.
     *
     * @return the detected intent
     */
    public IntentType detectedIntent() {
        return detectedIntent;
    }

    /**
     * Returns the capabilities required to fulfil the request, in suggested
     * execution order.
     *
     * @return an immutable list of capability requirements (never null)
     */
    public List<CapabilityRequirement> requiredCapabilities() {
        return requiredCapabilities;
    }

    /**
     * Returns the resolver's confidence in the detected intent, in {@code [0,1]}.
     *
     * @return the confidence score
     */
    public double confidence() {
        return confidence;
    }

    /**
     * Returns metadata describing how this plan was produced.
     *
     * @return an immutable metadata map (never null)
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Returns whether this plan requires the given capability.
     *
     * @param capability the capability to check
     * @return true if the capability is present in the plan
     */
    public boolean requires(CapabilityType capability) {
        return requiredCapabilities.stream()
                .anyMatch(r -> r.capability() == capability);
    }

    /**
     * Returns a new builder for {@link CapabilityPlan}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link CapabilityPlan}.
     */
    public static final class Builder {

        private String requestId;
        private IntentType detectedIntent = IntentType.CHAT;
        private final List<CapabilityRequirement> requiredCapabilities = new ArrayList<>();
        private double confidence = 0.0;
        private final Map<String, Object> metadata = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder detectedIntent(IntentType detectedIntent) {
            this.detectedIntent = detectedIntent;
            return this;
        }

        public Builder addRequirement(CapabilityRequirement requirement) {
            this.requiredCapabilities.add(
                    Objects.requireNonNull(requirement, "requirement must not be null"));
            return this;
        }

        public Builder requirements(List<CapabilityRequirement> requirements) {
            this.requiredCapabilities.addAll(
                    Objects.requireNonNull(requirements, "requirements must not be null"));
            return this;
        }

        public Builder confidence(double confidence) {
            if (confidence < 0.0 || confidence > 1.0) {
                throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
            }
            this.confidence = confidence;
            return this;
        }

        public Builder metadata(String key, Object value) {
            this.metadata.put(
                    Objects.requireNonNull(key, "key must not be null"), value);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata.putAll(
                    Objects.requireNonNull(metadata, "metadata must not be null"));
            return this;
        }

        public CapabilityPlan build() {
            return new CapabilityPlan(this);
        }
    }

    @Override
    public String toString() {
        return "CapabilityPlan{"
                + "requestId='" + requestId + '\''
                + ", detectedIntent=" + detectedIntent
                + ", requiredCapabilities=" + requiredCapabilities
                + ", confidence=" + confidence
                + ", metadata=" + metadata
                + '}';
    }
}