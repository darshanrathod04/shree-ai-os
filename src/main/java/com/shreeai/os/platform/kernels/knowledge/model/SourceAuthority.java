package com.shreeai.os.platform.kernels.knowledge.model;

import java.util.Locale;

/**
 * <b>SourceAuthority</b>
 *
 * <p>The closed, deterministic authority families of the K5 reliability
 * pipeline, each carrying its locked authority score. Authority is read from
 * the registered {@code KnowledgeSource} metadata - it is never inferred.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K5 Reliability and Freshness</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum SourceAuthority {

    /** Official documentation, published by the technology owner. */
    OFFICIAL(1.00),

    /** Enterprise-internal documentation with organizational review. */
    ENTERPRISE(0.95),

    /** Verified internal documentation curated by a trusted process. */
    VERIFIED(0.90),

    /** Community-authored documentation without formal review. */
    COMMUNITY(0.70),

    /** Unknown or undeclared authority. */
    UNKNOWN(0.40);

    private final double authorityScore;

    SourceAuthority(double authorityScore) {
        this.authorityScore = authorityScore;
    }

    /**
     * Returns the locked authority score of this family.
     *
     * @return the authority score within {@code [0.40, 1.00]}
     */
    public double authorityScore() {
        return authorityScore;
    }

    /**
     * Maps a declared authority label (typically the {@code authority} entry
     * of the source metadata) to its family. Matching is lowercase and
     * exact; anything unrecognized - including null - maps to
     * {@link #UNKNOWN}.
     *
     * @param label the declared authority label (may be null)
     * @return the matching family (never null)
     */
    public static SourceAuthority fromLabel(String label) {
        if (label == null || label.isBlank()) {
            return UNKNOWN;
        }
        return switch (label.trim().toLowerCase(Locale.ROOT)) {
            case "official" -> OFFICIAL;
            case "enterprise" -> ENTERPRISE;
            case "verified" -> VERIFIED;
            case "community" -> COMMUNITY;
            default -> UNKNOWN;
        };
    }
}
