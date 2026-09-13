package com.shreeai.os.platform.kernels.context.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>AmbiguityProfile</b>
 *
 * <p>Canonical, immutable artifact produced by the ambiguity diagnosis stage.
 * It answers the question: <em>"Is the request structurally complete?"</em></p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Aggregates every diagnosed {@link AmbiguityReason} into one object.</li>
 *   <li>Provides a deterministic ambiguity score in the range 0.0 - 1.0.</li>
 *   <li>Diagnoses only - it does <b>not</b> carry a clarification decision
 *       (that is a Chief Intelligence orchestration decision).</li>
 * </ul>
 *
 * <p><b>Determinism:</b> Same input artifacts produce an identical profile.
 * No timestamps or randomness are stored.</p>
 *
 * <p><b>Ownership:</b> Context Kernel - P1 Context Intelligence</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param ambiguityDetected true when at least one ambiguity risk was diagnosed
 * @param reasons the diagnosed reasons (never null, defensively copied)
 * @param ambiguityScore the clamped aggregate score (0.0 - 1.0)
 * @param explanation a deterministic human-readable summary (never null)
 */
public record AmbiguityProfile(
        boolean ambiguityDetected,
        List<AmbiguityReason> reasons,
        double ambiguityScore,
        String explanation) {

    /**
     * Compact constructor that defensively copies the reasons list so the
     * profile is deeply immutable.
     *
     * @throws NullPointerException if reasons or explanation is null
     */
    public AmbiguityProfile {
        Objects.requireNonNull(reasons, "reasons must not be null");
        Objects.requireNonNull(explanation, "explanation must not be null");
        reasons = List.copyOf(reasons);
    }

    /**
     * Returns a profile representing a structurally complete request.
     *
     * @return a clean profile with no ambiguity
     */
    public static AmbiguityProfile none() {
        return new AmbiguityProfile(false, List.of(), 0.0,
                "No ambiguity detected - the request is structurally complete.");
    }

    @Override
    public String toString() {
        return String.format("AmbiguityProfile{ambiguityDetected=%s, reasons=%d, score=%.2f}",
                ambiguityDetected, reasons.size(), ambiguityScore);
    }
}