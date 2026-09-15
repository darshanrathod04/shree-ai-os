package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>VerificationIssue</b>
 *
 * <p>An immutable, deterministic issue discovered during self-verification.
 * Each issue is tied to a specific reasoning node and carries a locked
 * severity value from {@link VerificationIssueType}.
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R4 Self Verification</p>
 * <p><b>Version:</b> 1.0</p>
 */
public record VerificationIssue(
        VerificationIssueType type,
        String nodeId,
        String explanation,
        double severity
) {

    /**
     * Compact constructor validating all fields.
     *
     * @throws NullPointerException     if type nodeId or explanation is null
     * @throws IllegalArgumentException if nodeId or explanation is blank or
     *                                  severity is not one of {0.25, 0.50, 0.75, 1.00}
     */
    public VerificationIssue {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(nodeId, "nodeId must not be null");
        Objects.requireNonNull(explanation, "explanation must not be null");
        if (nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId must not be blank");
        }
        if (explanation.isBlank()) {
            throw new IllegalArgumentException("explanation must not be blank");
        }
        // Validate severity is one of the locked values
        double clamped = Math.round(severity * 100.0) / 100.0;
        if (clamped != 0.25 && clamped != 0.50 && clamped != 0.75 && clamped != 1.00) {
            throw new IllegalArgumentException(
                    "severity must be 0.25, 0.50, 0.75 or 1.00: " + severity);
        }
    }

    /**
     * Returns the locked severity for this issue type,
     * overriding any caller-provided value.
     */
    public double severity() {
        return type.severity();
    }
}
