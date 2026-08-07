package com.shreeai.os.platform.kernels.inference.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>InferenceResult</b>
 *
 * <p>Represents the result of an inference operation.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 */
public record InferenceResult(
        String inferenceId,
        List<Hypothesis> hypotheses,
        Hypothesis bestHypothesis,
        double confidence,
        List<String> supportingEvidence,
        List<String> contradictingEvidence,
        List<String> unknownInformation,
        String recommendedNextInvestigation,
        String context,
        Instant generatedAt
) {
    public InferenceResult {
        Objects.requireNonNull(inferenceId, "inferenceId must not be null");
        Objects.requireNonNull(hypotheses, "hypotheses must not be null");
        Objects.requireNonNull(bestHypothesis, "bestHypothesis must not be null");
        Objects.requireNonNull(supportingEvidence, "supportingEvidence must not be null");
        Objects.requireNonNull(contradictingEvidence, "contradictingEvidence must not be null");
        Objects.requireNonNull(unknownInformation, "unknownInformation must not be null");
        Objects.requireNonNull(recommendedNextInvestigation, "recommendedNextInvestigation must not be null");
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(generatedAt, "generatedAt must not be null");
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
        }
        hypotheses = List.copyOf(hypotheses);
        supportingEvidence = List.copyOf(supportingEvidence);
        contradictingEvidence = List.copyOf(contradictingEvidence);
        unknownInformation = List.copyOf(unknownInformation);
    }
}