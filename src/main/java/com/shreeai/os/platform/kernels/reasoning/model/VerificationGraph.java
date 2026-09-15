package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>VerificationGraph</b>
 *
 * <p>The canonical verification artifact of R4: a stably-ordered
 * collection of verified nodes, deterministic issues and a locked
 * verification score.
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R4 Self Verification</p>
 * <p><b>Version:</b> 1.0</p>
 */
public record VerificationGraph(
        List<VerificationNode> nodes,
        List<VerificationIssue> issues,
        double verificationScore
) {

    /**
     * Compact constructor validating all fields.
     */
    public VerificationGraph {
        Objects.requireNonNull(nodes, "nodes must not be null");
        Objects.requireNonNull(issues, "issues must not be null");
        if (verificationScore < 0.0 || verificationScore > 1.0) {
            throw new IllegalArgumentException(
                    "verificationScore must be in [0,1]: " + verificationScore);
        }
        nodes = List.copyOf(nodes);
        issues = List.copyOf(issues);
    }
}
