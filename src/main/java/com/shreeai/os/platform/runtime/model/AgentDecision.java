package com.shreeai.os.platform.runtime.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>AgentDecision</b>
 *
 * <p>Immutable per-agent decision log entry. Each agent
 * ({@code ChiefIntelligenceAgent}, {@code DiagnosisAgent}, etc.) emits
 * one {@code AgentDecision} explaining its action, rationale, and the
 * confidence it assigns to the decision.</p>
 *
 * @since Sprint 18
 */
public final class AgentDecision {

    /** Logical identifier of the agent that produced this decision. */
    public enum Agent {
        CHIEF_INTELLIGENCE,
        DIAGNOSIS,
        EVIDENCE,
        VERIFICATION,
        NATURAL_RESPONSE
    }

    /** High-level action the agent took. */
    public enum Action {
        ROUTE,
        DIAGNOSE,
        EXTRACT,
        VERIFY,
        GENERATE,
        SHORT_CIRCUIT,
        NO_OP
    }

    private final String decisionId;
    private final Agent agent;
    private final Action action;
    private final String rationale;
    private final double confidence;
    private final Map<String, Object> metadata;
    private final long decidedAtMillis;

    private AgentDecision(Builder b) {
        this.decisionId = Objects.requireNonNull(b.decisionId, "decisionId must not be null");
        this.agent = Objects.requireNonNull(b.agent, "agent must not be null");
        this.action = Objects.requireNonNull(b.action, "action must not be null");
        this.rationale = Objects.requireNonNull(b.rationale, "rationale must not be null");
        this.confidence = clamp(b.confidence);
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(b.metadata));
        this.decidedAtMillis = b.decidedAtMillis;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String decisionId() { return decisionId; }
    public Agent agent() { return agent; }
    public Action action() { return action; }
    public String rationale() { return rationale; }
    public double confidence() { return confidence; }
    public Map<String, Object> metadata() { return metadata; }
    public long decidedAtMillis() { return decidedAtMillis; }

    private static double clamp(double v) {
        if (Double.isNaN(v)) return 0.0;
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgentDecision that)) return false;
        return Double.compare(that.confidence, confidence) == 0
                && decidedAtMillis == that.decidedAtMillis
                && Objects.equals(decisionId, that.decisionId)
                && agent == that.agent
                && action == that.action
                && Objects.equals(rationale, that.rationale)
                && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(decisionId, agent, action, rationale, confidence, metadata, decidedAtMillis);
    }

    @Override
    public String toString() {
        return "AgentDecision{decisionId='" + decisionId
                + "', agent=" + agent
                + ", action=" + action
                + ", confidence=" + confidence + '}';
    }

    public static final class Builder {
        private String decisionId = "dec-" + java.util.UUID.randomUUID();
        private Agent agent;
        private Action action = Action.NO_OP;
        private String rationale = "";
        private double confidence = 0.0;
        private Map<String, Object> metadata = new LinkedHashMap<>();
        private long decidedAtMillis = System.currentTimeMillis();

        public Builder decisionId(String decisionId) {
            this.decisionId = decisionId;
            return this;
        }

        public Builder agent(Agent agent) {
            this.agent = agent;
            return this;
        }

        public Builder action(Action action) {
            this.action = action;
            return this;
        }

        public Builder rationale(String rationale) {
            this.rationale = rationale;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = new LinkedHashMap<>(metadata);
            return this;
        }

        public Builder addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Builder decidedAtMillis(long decidedAtMillis) {
            this.decidedAtMillis = decidedAtMillis;
            return this;
        }

        public AgentDecision build() {
            return new AgentDecision(this);
        }
    }
}
