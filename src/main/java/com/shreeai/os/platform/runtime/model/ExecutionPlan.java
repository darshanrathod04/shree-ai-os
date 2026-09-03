package com.shreeai.os.platform.runtime.model;

import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult.IntentType;
import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult.KernelType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ExecutionPlan</b>
 *
 * <p>Immutable strategic plan produced by {@code ChiefIntelligenceAgent}
 * before any kernel stage is dispatched. Encapsulates the routing decision
 * for a single user request.</p>
 *
 * <p><b>Architectural Responsibility (Sprint 18):</b></p>
 * <ul>
 *   <li>Declares which kernels will run, in what order.</li>
 *   <li>Declares which kernels are skipped and why.</li>
 *   <li>Captures the detected {@link IntentType} for downstream agents.</li>
 *   <li>Carries routing metadata for observability and verification.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final.</li>
 *   <li>Builder-only construction — no public constructors.</li>
 *   <li>Defensive copies — protects mutable collections.</li>
 *   <li>Value-based equality — implements equals, hashCode, toString.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime / Autonomous Intelligence Layer</p>
 * <p><b>Version:</b> 1.0 (Sprint 18)</p>
 *
 * @since Sprint 18
 */
public final class ExecutionPlan {

    private final String planId;
    private final IntentType detectedIntent;
    private final List<KernelType> orderedKernels;
    private final Map<KernelType, String> skipReasons;
    private final Map<String, Object> routingMetadata;
    private final long createdAtMillis;

    private ExecutionPlan(Builder b) {
        this.planId = Objects.requireNonNull(b.planId, "planId must not be null");
        this.detectedIntent = Objects.requireNonNull(b.detectedIntent, "detectedIntent must not be null");
        this.orderedKernels = List.copyOf(b.orderedKernels);
        this.skipReasons = Collections.unmodifiableMap(new LinkedHashMap<>(b.skipReasons));
        this.routingMetadata = Collections.unmodifiableMap(new LinkedHashMap<>(b.routingMetadata));
        this.createdAtMillis = b.createdAtMillis;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String planId() { return planId; }
    public IntentType detectedIntent() { return detectedIntent; }
    public List<KernelType> orderedKernels() { return orderedKernels; }
    public Map<KernelType, String> skipReasons() { return skipReasons; }
    public Map<String, Object> routingMetadata() { return routingMetadata; }
    public long createdAtMillis() { return createdAtMillis; }

    /**
     * @return true when no kernels are ordered to run.
     */
    public boolean isEmpty() {
        return orderedKernels.isEmpty();
    }

    /**
     * @return true when at least one kernel is ordered to run.
     */
    public boolean hasKernels() {
        return !orderedKernels.isEmpty();
    }

    /**
     * @return true when every required kernel has been skipped.
     */
    public boolean allBlocked() {
        return orderedKernels.isEmpty();
    }

    /**
     * @return true when at least one required kernel was skipped.
     */
    public boolean hasSkippedKernels() {
        return !skipReasons.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExecutionPlan that)) return false;
        return createdAtMillis == that.createdAtMillis
                && Objects.equals(planId, that.planId)
                && detectedIntent == that.detectedIntent
                && Objects.equals(orderedKernels, that.orderedKernels)
                && Objects.equals(skipReasons, that.skipReasons)
                && Objects.equals(routingMetadata, that.routingMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, detectedIntent, orderedKernels, skipReasons, routingMetadata, createdAtMillis);
    }

    @Override
    public String toString() {
        return "ExecutionPlan{"
                + "planId='" + planId + '\''
                + ", detectedIntent=" + detectedIntent
                + ", orderedKernels=" + orderedKernels
                + ", skipReasons=" + skipReasons
                + '}';
    }

    public static final class Builder {
        private String planId = "plan-" + java.util.UUID.randomUUID();
        private IntentType detectedIntent = IntentType.CHAT;
        private List<KernelType> orderedKernels = new ArrayList<>();
        private Map<KernelType, String> skipReasons = new LinkedHashMap<>();
        private Map<String, Object> routingMetadata = new LinkedHashMap<>();
        private long createdAtMillis = System.currentTimeMillis();

        public Builder planId(String planId) {
            this.planId = planId;
            return this;
        }

        public Builder detectedIntent(IntentType detectedIntent) {
            this.detectedIntent = detectedIntent;
            return this;
        }

        public Builder orderedKernels(List<KernelType> orderedKernels) {
            this.orderedKernels = new ArrayList<>(orderedKernels);
            return this;
        }

        public Builder addKernel(KernelType kernel) {
            this.orderedKernels.add(kernel);
            return this;
        }

        public Builder skipKernel(KernelType kernel, String reason) {
            this.skipReasons.put(kernel, reason);
            this.orderedKernels.remove(kernel);
            return this;
        }

        public Builder routingMetadata(Map<String, Object> routingMetadata) {
            this.routingMetadata = new LinkedHashMap<>(routingMetadata);
            return this;
        }

        public Builder addMetadata(String key, Object value) {
            this.routingMetadata.put(key, value);
            return this;
        }

        public Builder createdAtMillis(long createdAtMillis) {
            this.createdAtMillis = createdAtMillis;
            return this;
        }

        public ExecutionPlan build() {
            return new ExecutionPlan(this);
        }
    }
}
