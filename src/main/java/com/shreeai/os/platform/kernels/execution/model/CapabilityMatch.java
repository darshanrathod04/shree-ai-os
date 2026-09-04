package com.shreeai.os.platform.kernels.execution.model;

public final class CapabilityMatch {
    private final Capability capability;
    private final double confidence;
    private final String reason;

    private CapabilityMatch(Capability capability, double confidence, String reason) {
        this.capability = capability;
        this.confidence = confidence;
        this.reason = reason != null ? reason : "";
    }

    public Capability getCapability() { return capability; }
    public double getConfidence() { return confidence; }
    public String getReason() { return reason; }

    public static CapabilityMatch of(Capability capability, double confidence, String reason) {
        return new CapabilityMatch(capability, confidence, reason);
    }

    @Override public String toString() {
        return "CapabilityMatch{capability=" + (capability != null ? capability.getName() : "null") + ", confidence=" + confidence + "}";
    }
}
