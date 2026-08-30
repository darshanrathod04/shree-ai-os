package com.shreeai.os.platform.intelligence.routing;

import com.shreeai.os.platform.intelligence.agent.Agent;
import com.shreeai.os.platform.intelligence.agent.AgentCapability;

import java.util.Objects;

/**
 * <b>RoutingTarget</b>
 *
 * <p>Immutable result of a context-aware routing decision: the selected
 * {@link Agent}, the matched {@link AgentCapability}, a confidence score in
 * [0.0, 1.0], and a human-readable reason.</p>
 *
 * <p><b>Ownership:</b> Intelligence — Context-aware Routing</p>
 * <p><b>Version:</b> 3.0</p>
 *
 * @since 3.0
 */
public final class RoutingTarget {

    private final Agent agent;
    private final AgentCapability matchedCapability;
    private final double confidence;
    private final String reason;

    private RoutingTarget(Builder builder) {
        this.agent = builder.agent;
        this.matchedCapability = builder.matchedCapability;
        this.confidence = builder.confidence;
        this.reason = builder.reason;
    }

    /** @return the selected agent (never null) */
    public Agent agent() {
        return agent;
    }

    /** @return the capability that matched (may be null) */
    public AgentCapability matchedCapability() {
        return matchedCapability;
    }

    /** @return the routing confidence in [0.0, 1.0] */
    public double confidence() {
        return confidence;
    }

    /** @return the routing reason (never null) */
    public String reason() {
        return reason;
    }

    @Override
    public String toString() {
        return "RoutingTarget{agent=" + agent.id() + ", matchedCapability="
                + matchedCapability + ", confidence=" + confidence + '}';
    }

    /** Creates a new builder. */
    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder for {@link RoutingTarget}. */
    public static final class Builder {

        private Agent agent;
        private AgentCapability matchedCapability;
        private double confidence;
        private String reason = "";

        private Builder() {
        }

        public Builder agent(Agent agent) {
            this.agent = Objects.requireNonNull(agent, "agent must not be null");
            return this;
        }

        public Builder matchedCapability(AgentCapability matchedCapability) {
            this.matchedCapability = matchedCapability;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = Math.max(0.0, Math.min(1.0, confidence));
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason == null ? "" : reason;
            return this;
        }

        public RoutingTarget build() {
            if (agent == null) {
                throw new IllegalArgumentException("agent must not be null");
            }
            return new RoutingTarget(this);
        }
    }
}
