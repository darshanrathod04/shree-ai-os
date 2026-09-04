package com.shreeai.os.platform.runtime.orchestration;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>IntentAnalysisResult</b>
 *
 * <p>Immutable result of intent analysis. Produced by {@link IntentAnalyzer}.
 * Contains the primary intent, secondary intents, required kernels, confidence,
 * and extracted entities.</p>
 *
 * <p>This is an internal runtime model — not exposed in the public SDK.</p>
 *
 * @since Sprint-12
 */
public final class IntentAnalysisResult {

    private final IntentType primaryIntent;
    private final List<IntentType> secondaryIntents;
    private final List<KernelType> requiredKernels;
    private final double confidence;
    private final Map<String, String> entities;
    private final String originalInput;

    private IntentAnalysisResult(
            IntentType primaryIntent,
            List<IntentType> secondaryIntents,
            List<KernelType> requiredKernels,
            double confidence,
            Map<String, String> entities,
            String originalInput
    ) {
        this.primaryIntent = primaryIntent;
        this.secondaryIntents = List.copyOf(secondaryIntents);
        this.requiredKernels = List.copyOf(requiredKernels);
        this.confidence = confidence;
        this.entities = Map.copyOf(entities);
        this.originalInput = Objects.requireNonNull(originalInput);
    }

    /** Creates a result via the builder. */
    public static Builder builder() {
        return new Builder();
    }

    /** @return the primary intent (never null) */
    public IntentType primaryIntent() {
        return primaryIntent;
    }

    /** @return immutable secondary intents (never null, may be empty) */
    public List<IntentType> secondaryIntents() {
        return secondaryIntents;
    }

    /** @return immutable required kernels (never null, always at least the primary) */
    public List<KernelType> requiredKernels() {
        return requiredKernels;
    }

    /** @return confidence score in [0.0, 1.0] */
    public double confidence() {
        return confidence;
    }

    /** @return immutable extracted entities (never null) */
    public Map<String, String> entities() {
        return entities;
    }

    /** @return the original user input (never null) */
    public String originalInput() {
        return originalInput;
    }

    /** @return true when multiple kernels are required */
    public boolean isMultiKernel() {
        return requiredKernels.size() > 1;
    }

    @Override
    public String toString() {
        return "IntentAnalysisResult{"
                + "primaryIntent=" + primaryIntent
                + ", secondaryIntents=" + secondaryIntents
                + ", requiredKernels=" + requiredKernels
                + ", confidence=" + confidence
                + ", entities=" + entities
                + '}';
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Intent Types
    // ─────────────────────────────────────────────────────────────────────────

    public enum IntentType {
        MEMORY_STORE,
        MEMORY_RECALL,
        PLANNING,
        KNOWLEDGE_QUERY,
        KNOWLEDGE_SEARCH,
        EXECUTION,
        REFLECTION,
        /** Sprint-14: Developer Agent — implementation planning & impact analysis */
        DEVELOPER,
        /** Sprint-17.3: Project Intelligence — actual code analysis on analyzed projects */
        PROJECT_INTELLIGENCE,
        CHAT
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Kernel Types
    // ─────────────────────────────────────────────────────────────────────────

    public enum KernelType {
        MEMORY,
        PLANNING,
        KNOWLEDGE,
        EXECUTION,
        REFLECTION,
        /** Sprint-14: Developer Agent kernel */
        DEVELOPER,
        /** Sprint-17.3: Project Intelligence kernel — operates on analyzed project structures */
        PROJECT,
        CHIEF
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Builder
    // ─────────────────────────────────────────────────────────────────────────

    public static final class Builder {
        private IntentType primaryIntent;
        private List<IntentType> secondaryIntents = List.of();
        private List<KernelType> requiredKernels = List.of();
        private double confidence = 0.0;
        private Map<String, String> entities = Map.of();
        private String originalInput = "";

        public Builder primaryIntent(IntentType primaryIntent) {
            this.primaryIntent = primaryIntent;
            return this;
        }

        public Builder secondaryIntents(List<IntentType> secondaryIntents) {
            this.secondaryIntents = List.copyOf(secondaryIntents);
            return this;
        }

        public Builder requiredKernels(List<KernelType> requiredKernels) {
            this.requiredKernels = List.copyOf(requiredKernels);
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = Math.max(0.0, Math.min(1.0, confidence));
            return this;
        }

        public Builder entities(Map<String, String> entities) {
            this.entities = Map.copyOf(entities);
            return this;
        }

        public Builder originalInput(String originalInput) {
            this.originalInput = Objects.requireNonNull(originalInput);
            return this;
        }

        public IntentAnalysisResult build() {
            Objects.requireNonNull(primaryIntent, "primaryIntent must not be null");
            if (requiredKernels.isEmpty()) {
                requiredKernels = List.of(kernelFor(primaryIntent));
            }
            return new IntentAnalysisResult(
                    primaryIntent, secondaryIntents, requiredKernels,
                    confidence, entities, originalInput
            );
        }

        private static KernelType kernelFor(IntentType intent) {
            return switch (intent) {
                case MEMORY_STORE, MEMORY_RECALL -> KernelType.MEMORY;
                case PLANNING -> KernelType.PLANNING;
                case KNOWLEDGE_QUERY, KNOWLEDGE_SEARCH -> KernelType.KNOWLEDGE;
                case EXECUTION -> KernelType.EXECUTION;
                case REFLECTION -> KernelType.REFLECTION;
                case DEVELOPER -> KernelType.DEVELOPER;
                case PROJECT_INTELLIGENCE -> KernelType.PROJECT;   // Sprint-17.3
                case CHAT -> KernelType.CHIEF;
            };
        }
    }
}
