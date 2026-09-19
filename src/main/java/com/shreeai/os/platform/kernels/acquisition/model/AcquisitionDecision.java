package com.shreeai.os.platform.kernels.acquisition.model;

import com.shreeai.os.platform.kernels.knowledge.model.FreshnessLevel;

import java.util.Objects;

/**
 * <b>AcquisitionDecision</b>
 *
 * <p>The closed, deterministic cache decisions of the K0.6.4 Freshness &amp;
 * Cache Policy Engine. A decision answers exactly one question for one selected
 * source and nothing more - <em>should Shree AI OS use cached knowledge or
 * acquire fresh knowledge?</em></p>
 *
 * <p><b>Locked decision semantics:</b></p>
 * <table border="1">
 *   <caption>Locked decision semantics</caption>
 *   <tr><th>Decision</th><th>Meaning</th></tr>
 *   <tr><td>{@link #USE_CACHE}</td><td>Reuse the knowledge already held.</td></tr>
 *   <tr><td>{@link #REFRESH}</td><td>Re-acquire updated knowledge.</td></tr>
 *   <tr><td>{@link #ACQUIRE}</td><td>First-time ingestion.</td></tr>
 * </table>
 *
 * <p><b>Kernel consistency:</b> the locked cache policy is expressed as a pure
 * mapping from the K5 freshness vocabulary ({@link FreshnessLevel}), so K0.6.4
 * never re-invents or contradicts the locked freshness buckets.</p>
 *
 * <p><b>Immutability:</b> Enums are inherently immutable.</p>
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.4 Freshness &amp; Cache Policy</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum AcquisitionDecision {

    /** Reuse the knowledge already held - the cache is valid. */
    USE_CACHE,

    /** Re-acquire updated knowledge - the cache has expired. */
    REFRESH,

    /** First-time ingestion - no usable cached knowledge exists. */
    ACQUIRE;

    /**
     * <b>Locked cache policy.</b> Maps a K5 freshness level to its cache
     * decision:
     *
     * <ul>
     *   <li>{@link FreshnessLevel#LATEST} (0-30 days) -&gt; {@link #USE_CACHE}</li>
     *   <li>{@link FreshnessLevel#CURRENT} (31-180 days) -&gt; {@link #USE_CACHE}</li>
     *   <li>{@link FreshnessLevel#RECENT} (181-365 days) -&gt; {@link #REFRESH}</li>
     *   <li>{@link FreshnessLevel#OUTDATED} (366-1095 days) -&gt; {@link #REFRESH}</li>
     *   <li>{@link FreshnessLevel#ARCHIVED} (&gt;1095 days) -&gt; {@link #REFRESH}</li>
     * </ul>
     *
     * <p>The mapping is a fixed constant of the platform - it is never weighted,
     * tuned or inferred at request time.</p>
     *
     * @param freshnessLevel the locked freshness bucket (must not be null)
     * @return the locked cache decision (never null)
     * @throws NullPointerException if freshnessLevel is null
     */
    public static AcquisitionDecision forFreshnessLevel(FreshnessLevel freshnessLevel) {
        Objects.requireNonNull(freshnessLevel, "freshnessLevel must not be null");
        return switch (freshnessLevel) {
            case LATEST, CURRENT -> USE_CACHE;
            case RECENT, OUTDATED, ARCHIVED -> REFRESH;
        };
    }

    /**
     * Returns true when this decision requires knowledge to be acquired -
     * either for the first time ({@link #ACQUIRE}) or again ({@link #REFRESH}).
     * Only these decisions are executed by K0.6.5.
     *
     * @return true when fresh knowledge must be obtained
     */
    public boolean requiresAcquisition() {
        return this != USE_CACHE;
    }

    /**
     * Returns true when this decision reuses knowledge already held.
     *
     * @return true when cached knowledge is reused
     */
    public boolean usesCache() {
        return this == USE_CACHE;
    }
}
