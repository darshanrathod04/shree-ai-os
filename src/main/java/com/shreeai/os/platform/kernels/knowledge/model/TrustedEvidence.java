package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.Objects;

/**
 * <b>TrustedEvidence</b>
 *
 * <p>One immutable pairing of a K4 {@link EvidenceItem} with the K5
 * {@link TrustScore} computed for it. The original evidence is never
 * modified - trust evaluation only annotates it.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K5 Reliability and Freshness</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param evidence the original, unmodified retrieval evidence (never null)
 * @param trust    its deterministic trust evaluation (never null)
 */
public record TrustedEvidence(EvidenceItem evidence, TrustScore trust) {

    /**
     * Compact constructor that validates both components.
     *
     * @throws NullPointerException if evidence or trust is null
     */
    public TrustedEvidence {
        Objects.requireNonNull(evidence, "evidence must not be null");
        Objects.requireNonNull(trust, "trust must not be null");
    }

    @Override
    public String toString() {
        return String.format("TrustedEvidence{chunkId=%s, trust=%s}",
                evidence.chunkId(), trust);
    }
}
