package com.shreeai.os.platform.kernels.cognitive.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ReasoningResult</b>
 *
 * <p>Represents the result of a reasoning operation within the Cognitive Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the complete output of a reasoning operation.</li>
 *   <li>Provides immutable reasoning results.</li>
 *   <li>Contains findings, conclusions, confidence, risks, and alternatives.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an immutable value object with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-101, EIO-ARCH-001</p>
 *
 * @param reasoningId the unique identifier for this reasoning result
 * @param summary the human-readable summary of reasoning
 * @param findings the list of findings from reasoning
 * @param evidence the list of evidence supporting conclusions
 * @param conclusion the derived conclusion (not retrieved)
 * @param confidence the confidence score (0.0 - 1.0)
 * @param risks the list of identified risks
 * @param alternatives the list of alternative conclusions
 * @param scope the scope of reasoning
 * @param reasoningType the type of reasoning applied
 * @param reasoningSteps the number of reasoning steps performed
 * @param metadata additional metadata
 * @param completedAt the timestamp when reasoning completed
 */
public record ReasoningResult(
        String reasoningId,
        String summary,
        List<String> findings,
        List<String> evidence,
        String conclusion,
        double confidence,
        List<String> risks,
        List<String> alternatives,
        String scope,
        String reasoningType,
        int reasoningSteps,
        Map<String, Object> metadata,
        Instant completedAt
) {
    /**
     * Creates a new ReasoningResult with validation.
     *
     * @param reasoningId the unique identifier (must not be null)
     * @param summary the human-readable summary (must not be null)
     * @param findings the list of findings (must not be null)
     * @param evidence the list of evidence (must not be null)
     * @param conclusion the derived conclusion (must not be null)
     * @param confidence the confidence score (0.0 - 1.0)
     * @param risks the list of risks (must not be null)
     * @param alternatives the list of alternatives (must not be null)
     * @param scope the scope of reasoning (must not be null)
     * @param reasoningType the type of reasoning (must not be null)
     * @param reasoningSteps the number of reasoning steps
     * @param metadata additional metadata (must not be null)
     * @param completedAt the completion timestamp (must not be null)
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if confidence is outside [0.0, 1.0]
     */
    public ReasoningResult {
        Objects.requireNonNull(reasoningId, "reasoningId must not be null");
        Objects.requireNonNull(summary, "summary must not be null");
        Objects.requireNonNull(findings, "findings must not be null");
        Objects.requireNonNull(evidence, "evidence must not be null");
        Objects.requireNonNull(conclusion, "conclusion must not be null");
        Objects.requireNonNull(risks, "risks must not be null");
        Objects.requireNonNull(alternatives, "alternatives must not be null");
        Objects.requireNonNull(scope, "scope must not be null");
        Objects.requireNonNull(reasoningType, "reasoningType must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(completedAt, "completedAt must not be null");
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
        }
        findings = List.copyOf(findings);
        evidence = List.copyOf(evidence);
        risks = List.copyOf(risks);
        alternatives = List.copyOf(alternatives);
        metadata = Map.copyOf(metadata);
    }
}