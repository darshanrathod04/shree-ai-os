package com.shreeai.os.platform.kernels.inference.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>Hypothesis</b>
 *
 * <p>Represents a hypothesis generated during inference.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates a single hypothesis with confidence and evidence</li>
 *   <li>Provides supporting and opposing evidence</li>
 *   <li>Contains status and priority</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Inference Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-INF-101</p>
 */
public record Hypothesis(
        String id,
        String description,
        double confidence,
        List<String> supportingEvidence,
        List<String> opposingEvidence,
        String status,
        int priority
) {
    /**
     * Creates a new Hypothesis with validation.
     */
    public Hypothesis {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(supportingEvidence, "supportingEvidence must not be null");
        Objects.requireNonNull(opposingEvidence, "opposingEvidence must not be null");
        Objects.requireNonNull(status, "status must not be null");
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
        }
        supportingEvidence = List.copyOf(supportingEvidence);
        opposingEvidence = List.copyOf(opposingEvidence);
    }
}