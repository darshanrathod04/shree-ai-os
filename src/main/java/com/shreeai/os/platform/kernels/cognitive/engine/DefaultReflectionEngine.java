package com.shreeai.os.platform.kernels.cognitive.engine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <b>DefaultReflectionEngine</b>
 *
 * <p>Evaluates the outcome of an execution after the fact: scores quality,
 * assigns a {@link ReflectionVerdict}, extracts actionable lessons and
 * advises on retry and memory persistence.</p>
 *
 * <p><b>Scoring model (0.0-1.0):</b></p>
 * <ul>
 *   <li>Execution success (50%) — did the action stage complete?</li>
 *   <li>Conclusion confidence (30%) — trust in the produced conclusion.</li>
 *   <li>Grounding/evidence (20%) — whether knowledge or evidence was
 *       attached to the execution.</li>
 * </ul>
 *
 * <p><b>Verdict thresholds:</b> SUCCESS &gt;= 0.75, PARTIAL &gt;= 0.4,
 * FAILURE below.</p>
 *
 * <p>The engine is deterministic and LLM-free, so it runs offline and in
 * tests; an LLM-backed evaluator can delegate to it through the runtime's
 * LLM router in a later engineering order.</p>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 * <p><b>Constitutional Authority:</b> EIO-COG-101</p>
 */
public final class DefaultReflectionEngine {

    private static final double SUCCESS_THRESHOLD = 0.75;
    private static final double PARTIAL_THRESHOLD = 0.40;

    /** Creates the engine. */
    public DefaultReflectionEngine() {
    }

    /**
     * Reflects on a completed execution.
     *
     * @param input the reflection input (must not be null)
     * @return the reflection analysis (never null)
     */
    public ReflectionAnalysis reflect(ReflectionInput input) {
        Objects.requireNonNull(input, "input must not be null");

        double score = score(input);
        ReflectionVerdict verdict = verdictOf(score);
        List<String> lessons = lessons(input, verdict);
        boolean memoryWorthy = !lessons.isEmpty();
        boolean retryAdvised = verdict == ReflectionVerdict.FAILURE;

        String summary = "Execution " + input.requestId() + " completed with verdict "
                + verdict + " (score " + String.format("%.2f", score) + ")";

        return new ReflectionAnalysis(
                verdict,
                score,
                lessons,
                summary,
                memoryWorthy,
                retryAdvised,
                Instant.now());
    }

    /**
     * Scores an execution's quality.
     *
     * @param input the reflection input (must not be null)
     * @return the quality score (0.0-1.0)
     */
    public double score(ReflectionInput input) {
        Objects.requireNonNull(input, "input must not be null");

        double execution = input.executionSuccess() ? 1.0 : 0.0;
        double confidence = clamp(input.confidence());
        double grounding = groundingFactor(input);

        return clamp(0.5 * execution + 0.3 * confidence + 0.2 * grounding);
    }

    private ReflectionVerdict verdictOf(double score) {
        if (score >= SUCCESS_THRESHOLD) {
            return ReflectionVerdict.SUCCESS;
        }
        if (score >= PARTIAL_THRESHOLD) {
            return ReflectionVerdict.PARTIAL;
        }
        return ReflectionVerdict.FAILURE;
    }

    private List<String> lessons(ReflectionInput input, ReflectionVerdict verdict) {
        List<String> lessons = new ArrayList<>();

        if (!input.executionSuccess()) {
            lessons.add("Execution did not complete for request '" + input.requestId()
                    + "'; review the action status '" + input.actionStatus() + "'.");
        }

        if (input.planStepCount() == 0) {
            lessons.add("Request executed without an explicit plan; planning may improve quality.");
        } else if (input.planStepCount() > 8) {
            lessons.add("Plan had " + input.planStepCount()
                    + " steps; consider decomposing large plans to reduce failure risk.");
        }

        if (input.confidence() < 0.5) {
            lessons.add("Conclusion confidence was low ("
                    + String.format("%.2f", clamp(input.confidence()))
                    + "); gather more evidence before acting on this result.");
        }

        if (groundingFactor(input) == 0.0) {
            lessons.add("No knowledge or evidence was attached; consider enriching retrieval.");
        }

        if (lessons.isEmpty() && verdict == ReflectionVerdict.SUCCESS) {
            lessons.add("Execution met expectations; reinforce the current strategy.");
        }

        return lessons;
    }

    private double groundingFactor(ReflectionInput input) {
        String summary = input.responseSummary();
        boolean hasSummary = summary != null && !summary.isBlank();
        boolean planned = input.planStepCount() > 0;
        return (hasSummary ? 0.5 : 0.0) + (planned ? 0.5 : 0.0);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}