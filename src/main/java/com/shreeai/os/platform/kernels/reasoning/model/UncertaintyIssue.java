package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.Objects;

/**
 * <b>UncertaintyIssue</b>
 *
 * <p>An immutable, deterministic issue discovered during uncertainty modeling.
 * Each issue is tied to a specific reasoning node id and carries a locked
 * impact value from {@code {0.25, 0.50, 0.75, 1.00}}.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R5 Uncertainty Modeling</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param type        the uncertainty issue type (never null)
 * @param nodeId      the deterministic node id the issue refers to (never null
 *                    or blank)
 * @param explanation the deterministic explanation (never null or blank)
 * @param impact      the locked impact value, one of {@code {0.25, 0.50, 0.75,
 *                    1.00}}
 */
public record UncertaintyIssue(
        UncertaintyType type,
        String nodeId,
        String explanation,
        double impact
) {

    /**
     * Compact constructor validating all fields.
     *
     * @throws NullPointerException     if type nodeId or explanation is null
     * @throws IllegalArgumentException if nodeId or explanation is blank or
     *                                  impact is not one of {0.25, 0.50, 0.75,
     *                                  1.00}
     */
    public UncertaintyIssue {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(nodeId, "nodeId must not be null");
        Objects.requireNonNull(explanation, "explanation must not be null");
        if (nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId must not be blank");
        }
        if (explanation.isBlank()) {
            throw new IllegalArgumentException("explanation must not be blank");
        }
        double clamped = Math.round(impact * 100.0) / 100.0;
        if (clamped != 0.25 && clamped != 0.50 && clamped != 0.75 && clamped != 1.00) {
            throw new IllegalArgumentException(
                    "impact must be 0.25, 0.50, 0.75 or 1.00: " + impact);
        }
    }

    @Override
    public String toString() {
        return String.format("UncertaintyIssue{type=%s, nodeId=%s, impact=%.2f}",
                type, nodeId, impact);
    }
}
