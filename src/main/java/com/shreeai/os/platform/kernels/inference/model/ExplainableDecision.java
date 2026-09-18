package com.shreeai.os.platform.kernels.inference.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>ExplainableDecision</b>
 *
 * <p>The canonical, immutable output artifact of the I5 Explainable Decision
 * Engine: the complete, structured, display-ready explanation of the
 * optimized decision - its goal, strategy, trade-offs, confidence and
 * evidence.</p>
 *
 * <p>This is the public artifact of the Inference Intelligence Kernel. It
 * carries pure structured data - no markdown, no HTML and no generated
 * prose. Any UI, SDK or LLM may later render it into English, Hindi, JSON,
 * PDF or any other presentation format.</p>
 *
 * <p><b>Immutability:</b> This record is deeply immutable - the section list
 * is defensively copied and unmodifiable, and every section is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I5 Explainable Decision</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param candidateId       the deterministic id of the explained candidate
 *                          (never null or blank)
 * @param strategy          the selected strategy type (never null)
 * @param optimizationScore the I3 optimization score within
 *                          {@code [0.0, 1.0]}
 * @param confidence        the I4 calibrated confidence within
 *                          {@code [0.0, 1.0]}
 * @param confidenceLevel   the I4 locked confidence level (never null)
 * @param sections          the ordered explanation sections in locked
 *                          {@link ExplanationSectionType} order (never null
 *                          or empty)
 */
public record ExplainableDecision(
        String candidateId,
        AlternativeType strategy,
        double optimizationScore,
        double confidence,
        ConfidenceLevel confidenceLevel,
        List<ExplanationSection> sections
) {

    /**
     * Creates a validated, deeply-immutable explainable decision.
     *
     * @throws NullPointerException     if any reference field is null
     * @throws IllegalArgumentException if candidateId is blank, either score
     *                                  is outside {@code [0.0, 1.0]}, or the
     *                                  section list is empty
     */
    public ExplainableDecision {
        Objects.requireNonNull(candidateId, "candidateId must not be null");
        Objects.requireNonNull(strategy, "strategy must not be null");
        Objects.requireNonNull(confidenceLevel, "confidenceLevel must not be null");
        Objects.requireNonNull(sections, "sections must not be null");
        if (candidateId.isBlank()) {
            throw new IllegalArgumentException("candidateId must not be blank");
        }
        if (Double.isNaN(optimizationScore) || optimizationScore < 0.0
                || optimizationScore > 1.0) {
            throw new IllegalArgumentException(
                    "optimizationScore must be within [0.0, 1.0]: " + optimizationScore);
        }
        if (Double.isNaN(confidence) || confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException(
                    "confidence must be within [0.0, 1.0]: " + confidence);
        }
        if (sections.isEmpty()) {
            throw new IllegalArgumentException("sections must not be empty");
        }
        sections = List.copyOf(sections);
    }

    @Override
    public String toString() {
        return "ExplainableDecision{candidateId='" + candidateId
                + "', strategy=" + strategy
                + ", optimizationScore=" + optimizationScore
                + ", confidence=" + confidence
                + ", confidenceLevel=" + confidenceLevel
                + ", sections=" + sections.size() + "}";
    }
}