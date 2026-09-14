package com.shreeai.os.platform.kernels.knowledge.model;

import java.time.Duration;

/**
 * <b>FreshnessLevel</b>
 *
 * <p>The closed, deterministic freshness families of the K5 reliability
 * pipeline, each carrying its locked freshness score. Freshness is computed
 * from document metadata only (the document ingestion timestamp against the
 * evaluation clock) - no web calls, no external lookups.</p>
 *
 * <p><b>Locked age buckets:</b> {@code LATEST} up to 30 days, {@code CURRENT}
 * up to 180 days, {@code RECENT} up to 365 days, {@code OUTDATED} up to 1095
 * days, {@code ARCHIVED} beyond that.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K5 Reliability and Freshness</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum FreshnessLevel {

    /** Ingested within the last 30 days. */
    LATEST(1.00),

    /** Ingested between 30 and 180 days ago. */
    CURRENT(0.90),

    /** Ingested between 180 and 365 days ago. */
    RECENT(0.75),

    /** Ingested between 365 and 1095 days ago. */
    OUTDATED(0.50),

    /** Ingested more than 1095 days ago, or of unknown age. */
    ARCHIVED(0.20);

    /** Locked bucket boundary in days: LATEST. */
    public static final long MAX_LATEST_DAYS = 30;

    /** Locked bucket boundary in days: CURRENT. */
    public static final long MAX_CURRENT_DAYS = 180;

    /** Locked bucket boundary in days: RECENT. */
    public static final long MAX_RECENT_DAYS = 365;

    /** Locked bucket boundary in days: OUTDATED. */
    public static final long MAX_OUTDATED_DAYS = 1095;

    private final double freshnessScore;

    FreshnessLevel(double freshnessScore) {
        this.freshnessScore = freshnessScore;
    }

    /**
     * Returns the locked freshness score of this level.
     *
     * @return the freshness score within {@code [0.20, 1.00]}
     */
    public double freshnessScore() {
        return freshnessScore;
    }

    /**
     * Buckets a document age deterministically. Negative ages (future
     * timestamps) are treated as zero.
     *
     * @param age the document age (must not be null)
     * @return the matching freshness level (never null)
     */
    public static FreshnessLevel ofAge(Duration age) {
        long days = Math.max(0, age.toDays());
        if (days <= MAX_LATEST_DAYS) {
            return LATEST;
        }
        if (days <= MAX_CURRENT_DAYS) {
            return CURRENT;
        }
        if (days <= MAX_RECENT_DAYS) {
            return RECENT;
        }
        if (days <= MAX_OUTDATED_DAYS) {
            return OUTDATED;
        }
        return ARCHIVED;
    }
}
