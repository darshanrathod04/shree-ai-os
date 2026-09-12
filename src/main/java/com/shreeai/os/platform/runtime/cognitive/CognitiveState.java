package com.shreeai.os.platform.runtime.cognitive;

import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionAnalysis;
import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.context.model.IntentProfile;
import com.shreeai.os.platform.kernels.inference.model.EvidencePackage;
import com.shreeai.os.platform.kernels.inference.model.InferenceResult;
import com.shreeai.os.platform.kernels.response.contracts.PlanningResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <b>CognitiveState</b>
 *
 * <p>Immutable value object holding the cognitive artifacts produced by the
 * pipeline's cognitive segment (Reasoning → Inference → Planning → Reflection)
 * together with the reflection loop bookkeeping (iteration count and quality
 * history).</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Single source of truth for cognitive artifacts during a pipeline run
 *       (replaces scattered {@code state.addMetadata(...)} cognitive keys).</li>
 *   <li>Every mutation returns a <em>new</em> instance - the previous state is
 *       never modified, so readers always observe a consistent snapshot.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable - no method mutates this object.</li>
 *   <li>Thread-safe - all fields are deeply immutable.</li>
 *   <li>No duplicated state - reasoning/inference/planning/reflection live
 *       here and nowhere else.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel - Cognitive Segment</p>
 *
  * @param reasoning           the reasoning result (null before ReasoningStage)
 * @param inference           the inference result (null before InferenceStage)
 * @param planning            the planning response (null before PlanningStage)
 * @param reflection          the latest reflection analysis (null before
 *                            ReflectionStage)
 * @param reflectionIteration the number of completed reflection passes
 * @param qualityHistory      quality scores recorded per reflection pass
 * @param evidencePackage     the resolved evidence package (null before InferenceStage)
 * @param intentProfile       the detected primary intent (null before ContextStage)
 */
public record CognitiveState(
        ReasoningResult reasoning,
        InferenceResult inference,
        PlanningResponse planning,
        ReflectionAnalysis reflection,
        int reflectionIteration,
        List<Double> qualityHistory,
        EvidencePackage evidencePackage,
        IntentProfile intentProfile) {

    /** Creates a deeply-immutable CognitiveState with defensive copies. */
    public CognitiveState {
        qualityHistory = qualityHistory == null
                ? List.of()
                : List.copyOf(qualityHistory);
    }

    /**
     * Returns the initial empty cognitive state (no artifacts, iteration 0).
     *
     * @return an empty state (never null)
     */
        public static CognitiveState empty() {
        return new CognitiveState(null, null, null, null, 0, List.of(), null, null);
    }

    /**
     * Returns a new state with the given reasoning result.
     *
     * @param reasoning the reasoning result (must not be null)
     * @return a new CognitiveState (never null)
     */
        public CognitiveState withReasoning(ReasoningResult reasoning) {
        Objects.requireNonNull(reasoning, "reasoning must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile);
    }

    /**
     * Returns a new state with the given inference result.
     *
     * @param inference the inference result (must not be null)
     * @return a new CognitiveState (never null)
     */
        public CognitiveState withInference(InferenceResult inference) {
        Objects.requireNonNull(inference, "inference must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile);
    }

    /**
     * Returns a new state with the given planning response.
     *
     * @param planning the planning response (must not be null)
     * @return a new CognitiveState (never null)
     */
        public CognitiveState withPlanning(PlanningResponse planning) {
        Objects.requireNonNull(planning, "planning must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile);
    }

    /**
     * Returns a new state with the given reflection analysis and quality
     * score, advancing the reflection iteration by one.
     *
     * @param reflection the reflection analysis (must not be null)
     * @param qualityScore the quality score recorded for this pass (0.0-1.0)
     * @return a new CognitiveState (never null)
     */
        public CognitiveState withReflection(ReflectionAnalysis reflection, double qualityScore) {
        Objects.requireNonNull(reflection, "reflection must not be null");
        List<Double> history = new ArrayList<>(qualityHistory);
        history.add(qualityScore);
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration + 1, List.copyOf(history), evidencePackage, intentProfile);
    }

        /**
     * Returns the canonical evidence package produced by the conflict resolver.
     * The evidence package is the authoritative source of evidence for the
     * inference kernel. It is not stored in metadata - it lives only inside
     * the immutable CognitiveState.
     *
     * @return the evidence package (null before InferenceStage resolves conflicts)
     */
    @Override
    public EvidencePackage evidencePackage() {
        return evidencePackage;
    }

    /**
     * Returns a new cognitive state with the given evidence package,
     * preserving all existing cognitive artifacts.
     *
     * @param pkg the resolved evidence package (must not be null)
     * @return a new CognitiveState with the evidence package set (never null)
     */
    public CognitiveState withEvidencePackage(EvidencePackage pkg) {
        Objects.requireNonNull(pkg, "evidencePackage must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, pkg, intentProfile);
    }

    /**
     * Returns a new state with the reflection iteration advanced by one,
     * without replacing the reflection analysis.
     *
     * @return a new CognitiveState (never null)
     */
        public CognitiveState incrementReflection() {
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration + 1, qualityHistory, evidencePackage, intentProfile);
    }

    /**
     * Returns a new cognitive state with the given intent profile,
     * preserving all existing cognitive artifacts.
     *
     * @param intentProfile the detected primary intent profile (must not be null)
     * @return a new CognitiveState with the intent profile set (never null)
     */
    public CognitiveState withIntentProfile(IntentProfile intentProfile) {
        Objects.requireNonNull(intentProfile, "intentProfile must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile);
    }

    /**
     * Returns the latest reflection quality score, or NaN when no reflection
     * pass has completed.
     *
     * @return the latest quality score (NaN when history is empty)
     */
    public double latestQualityScore() {
        return qualityHistory.isEmpty() ? Double.NaN : qualityHistory.get(qualityHistory.size() - 1);
    }

    @Override
    public String toString() {
        return "CognitiveState{reasoning=" + (reasoning != null)
                + ", inference=" + (inference != null)
                + ", planning=" + (planning != null)
                + ", reflection=" + (reflection != null)
                + ", reflectionIteration=" + reflectionIteration
                                 + ", qualityHistory=" + qualityHistory
                + ", evidencePackage=" + (evidencePackage != null)
                + ", intentProfile=" + intentProfile + '}';
    }
}
