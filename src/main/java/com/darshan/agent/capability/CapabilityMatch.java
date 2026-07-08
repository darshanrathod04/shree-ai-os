package com.darshan.agent.capability;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable result of a capability lookup.
 * Contains the matched capability and confidence information.
 */
public final class CapabilityMatch {

    private final Capability capability;
    private final double confidence;
    private final String matchedIntent;
    private final String reason;
    private final long processingTimeNanos;
    private final Instant timestamp;

    public CapabilityMatch(Capability capability, double confidence, String matchedIntent, String reason, long processingTimeNanos) {
        this.capability = capability;
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
        this.matchedIntent = Objects.requireNonNull(matchedIntent, "matchedIntent must not be null");
        this.reason = reason != null ? reason : "";
        this.processingTimeNanos = processingTimeNanos;
        this.timestamp = Instant.now();
    }

    public Capability getCapability() { return capability; }
    public double getConfidence() { return confidence; }
    public String getMatchedIntent() { return matchedIntent; }
    public String getReason() { return reason; }
    public long getProcessingTimeNanos() { return processingTimeNanos; }
    public Instant getTimestamp() { return timestamp; }

    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }

    @Override
    public String toString() {
        return String.format("CapabilityMatch{cap='%s', intent='%s', conf=%.0f%%}",
                capability != null ? capability.getName() : "null",
                matchedIntent, confidence * 100);
    }
}