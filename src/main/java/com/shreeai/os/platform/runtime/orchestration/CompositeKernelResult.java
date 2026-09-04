package com.shreeai.os.platform.runtime.orchestration;

import com.shreeai.os.platform.runtime.execution.RichExecutionResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>CompositeKernelResult</b>
 *
 * <p>Internal container that aggregates results from multiple kernel executions
 * in a multi-kernel orchestration. Contains all kernel outputs, execution order,
 * timing, confidence, citations, memories, and plans.</p>
 *
 * <p>This is an internal runtime model — not exposed in the public SDK.
 * It is used to pass multi-kernel results to the synthesizer.</p>
 *
 * @since Sprint-12
 */
public final class CompositeKernelResult {

    private final String requestId;
    private final List<KernelResult> kernelResults;
    private final List<String> executionOrder;
    private final Instant startedAt;
    private final Instant completedAt;
    private final Map<String, Object> citations;
    private final Map<String, Object> storedMemories;
    private final Map<String, Object> planData;
    private final Map<String, Object> reflectionData;
    private final double overallConfidence;
    private final boolean success;

    private CompositeKernelResult(Builder builder) {
        this.requestId = Objects.requireNonNull(builder.requestId);
        this.kernelResults = List.copyOf(builder.kernelResults);
        this.executionOrder = List.copyOf(builder.executionOrder);
        this.startedAt = builder.startedAt != null ? builder.startedAt : Instant.now();
        this.completedAt = builder.completedAt != null ? builder.completedAt : Instant.now();
        this.citations = Map.copyOf(builder.citations);
        this.storedMemories = Map.copyOf(builder.storedMemories);
        this.planData = Map.copyOf(builder.planData);
        this.reflectionData = Map.copyOf(builder.reflectionData);
        this.overallConfidence = Math.round(builder.overallConfidence * 100.0) / 100.0;
        this.success = builder.success;
    }

    public static Builder builder() {
        return new Builder();
    }

    // ─── Getters ────────────────────────────────────────────────────────────

    public String requestId() {
        return requestId;
    }

    public List<KernelResult> kernelResults() {
        return kernelResults;
    }

    public List<String> executionOrder() {
        return executionOrder;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant completedAt() {
        return completedAt;
    }

    public long durationMs() {
        return Math.max(0, completedAt.toEpochMilli() - startedAt.toEpochMilli());
    }

    public Map<String, Object> citations() {
        return citations;
    }

    public Map<String, Object> storedMemories() {
        return storedMemories;
    }

    public Map<String, Object> planData() {
        return planData;
    }

    public Map<String, Object> reflectionData() {
        return reflectionData;
    }

    public double overallConfidence() {
        return overallConfidence;
    }

    public boolean isSuccess() {
        return success;
    }

    /**
     * @return the primary output string (from the first kernel result)
     */
    public String primaryOutput() {
        if (kernelResults.isEmpty()) {
            return "";
        }
        return kernelResults.get(0).output();
    }

    /**
     * @return the last output string (from the last kernel result)
     */
    public String lastOutput() {
        if (kernelResults.isEmpty()) {
            return "";
        }
        return kernelResults.get(kernelResults.size() - 1).output();
    }

    @Override
    public String toString() {
        return "CompositeKernelResult{"
                + "requestId='" + requestId + '\''
                + ", kernelCount=" + kernelResults.size()
                + ", executionOrder=" + executionOrder
                + ", durationMs=" + durationMs()
                + ", overallConfidence=" + overallConfidence
                + ", success=" + success
                + '}';
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Kernel Result Entry
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Result from a single kernel execution within a composite result.
     */
    public static final class KernelResult {

        private final String kernelName;
        private final IntentAnalysisResult.KernelType kernelType;
        private final String output;
        private final boolean success;
        private final long executionTimeMs;
        private final double confidence;
        private final Map<String, Object> metadata;

        public KernelResult(
                String kernelName,
                IntentAnalysisResult.KernelType kernelType,
                String output,
                boolean success,
                long executionTimeMs,
                double confidence,
                Map<String, Object> metadata
        ) {
            this.kernelName = Objects.requireNonNull(kernelName);
            this.kernelType = Objects.requireNonNull(kernelType);
            this.output = output != null ? output : "";
            this.success = success;
            this.executionTimeMs = Math.max(0, executionTimeMs);
            this.confidence = Math.max(0.0, Math.min(1.0, confidence));
            this.metadata = Map.copyOf(metadata != null ? metadata : Map.of());
        }

        public String kernelName() {
            return kernelName;
        }

        public IntentAnalysisResult.KernelType kernelType() {
            return kernelType;
        }

        public String output() {
            return output;
        }

        public boolean isSuccess() {
            return success;
        }

        public long executionTimeMs() {
            return executionTimeMs;
        }

        public double confidence() {
            return confidence;
        }

        public Map<String, Object> metadata() {
            return metadata;
        }

        /**
         * Creates a KernelResult from a RichExecutionResult.
         */
        public static KernelResult fromRichResult(
                IntentAnalysisResult.KernelType kernelType,
                RichExecutionResult richResult
        ) {
            return new KernelResult(
                    kernelType.name() + " Kernel",
                    kernelType,
                    richResult.output(),
                    richResult.isSuccess(),
                    richResult.durationMs(),
                    richResult.confidence(),
                    richResult.metadata()
            );
        }

        @Override
        public String toString() {
            return "KernelResult{kernel=" + kernelName
                    + ", success=" + success
                    + ", executionTimeMs=" + executionTimeMs
                    + ", confidence=" + confidence + '}';
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Builder
    // ─────────────────────────────────────────────────────────────────────────

    public static final class Builder {
        private String requestId;
        private final List<KernelResult> kernelResults = new ArrayList<>();
        private final List<String> executionOrder = new ArrayList<>();
        private Instant startedAt;
        private Instant completedAt;
        private final Map<String, Object> citations = new LinkedHashMap<>();
        private final Map<String, Object> storedMemories = new LinkedHashMap<>();
        private final Map<String, Object> planData = new LinkedHashMap<>();
        private final Map<String, Object> reflectionData = new LinkedHashMap<>();
        private double overallConfidence = 0.0;
        private boolean success = true;

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder addKernelResult(KernelResult result) {
            this.kernelResults.add(Objects.requireNonNull(result));
            this.executionOrder.add(result.kernelType().name());
            if (!result.isSuccess()) {
                this.success = false;
            }
            return this;
        }

        public Builder startedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder addCitations(Map<String, Object> citations) {
            if (citations != null && !citations.isEmpty()) {
                this.citations.putAll(citations);
            }
            return this;
        }

        public Builder addStoredMemories(Map<String, Object> memories) {
            if (memories != null && !memories.isEmpty()) {
                this.storedMemories.putAll(memories);
            }
            return this;
        }

        public Builder addPlanData(Map<String, Object> planData) {
            if (planData != null && !planData.isEmpty()) {
                this.planData.putAll(planData);
            }
            return this;
        }

        public Builder addReflectionData(Map<String, Object> reflectionData) {
            if (reflectionData != null && !reflectionData.isEmpty()) {
                this.reflectionData.putAll(reflectionData);
            }
            return this;
        }

        public Builder overallConfidence(double overallConfidence) {
            this.overallConfidence = overallConfidence;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        /**
         * Computes the aggregate confidence as the average of all kernel confidences.
         */
        public Builder computeConfidenceFromResults() {
            if (kernelResults.isEmpty()) {
                this.overallConfidence = 0.0;
                return this;
            }
            double sum = 0.0;
            for (KernelResult r : kernelResults) {
                sum += r.confidence();
            }
            this.overallConfidence = sum / kernelResults.size();
            return this;
        }

        public CompositeKernelResult build() {
            Objects.requireNonNull(requestId, "requestId must not be null");
            return new CompositeKernelResult(this);
        }
    }
}
