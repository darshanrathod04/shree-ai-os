package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>VerificationNode</b>
 *
 * <p>An immutable node in a {@link VerificationGraph}, carrying the
 * verification status and supporting evidence ids for a single reasoning node.
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R4 Self Verification</p>
 * <p><b>Version:</b> 1.0</p>
 */
public record VerificationNode(
        String nodeId,
        VerificationStatus status,
        List<String> supportingEvidenceIds
) {

    /**
     * Compact constructor validating all fields.
     *
     * @throws NullPointerException     if nodeId status or supportingEvidenceIds is null
     * @throws IllegalArgumentException if nodeId is blank
     */
    public VerificationNode {
        Objects.requireNonNull(nodeId, "nodeId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(supportingEvidenceIds, "supportingEvidenceIds must not be null");
        if (nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId must not be blank");
        }
        supportingEvidenceIds = List.copyOf(supportingEvidenceIds);
    }
}
