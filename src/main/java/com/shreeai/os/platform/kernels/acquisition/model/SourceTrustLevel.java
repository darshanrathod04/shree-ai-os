package com.shreeai.os.platform.kernels.acquisition.model;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * <b>SourceTrustLevel</b>
 *
 * <p>The closed, deterministic trust families used by the K0.6.3 Trust &amp;
 * Source Selection Engine. Each family carries its <em>locked</em> authority
 * score; the score is never computed, inferred or weighted - it is a fixed
 * constant of the platform.</p>
 *
 * <p><b>Locked authority policy (never changes):</b></p>
 * <table border="1">
 *   <caption>Locked authority scores</caption>
 *   <tr><th>Trust level</th><th>Authority score</th></tr>
 *   <tr><td>OFFICIAL</td><td>1.00</td></tr>
 *   <tr><td>ENTERPRISE</td><td>0.95</td></tr>
 *   <tr><td>VERIFIED</td><td>0.90</td></tr>
 *   <tr><td>COMMUNITY</td><td>0.70</td></tr>
 *   <tr><td>UNKNOWN</td><td>0.40</td></tr>
 * </table>
 *
 * <p><b>Kernel consistency:</b> these scores and labels are byte-for-byte
 * compatible with the K5 reliability vocabulary
 * ({@code SourceAuthority}), so a single registry metadata declaration drives
 * both reliability evaluation (K5) and trusted source selection (K0.6.3).</p>
 *
 * <p><b>Immutability:</b> Enums are inherently immutable.</p>
 * <p><b>Thread Safety:</b> Enums are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.3 Trust &amp; Source Selection</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum SourceTrustLevel {

    /** Official documentation, published by the technology owner (1.00). */
    OFFICIAL(1.00),

    /** Verified documentation curated by a trusted process (0.90). */
    VERIFIED(0.90),

    /** Enterprise-internal documentation with organizational review (0.95). */
    ENTERPRISE(0.95),

    /** Community-authored documentation without formal review (0.70). */
    COMMUNITY(0.70),

    /** Unknown or undeclared trust (0.40). */
    UNKNOWN(0.40);

    private final double authorityScore;

    SourceTrustLevel(double authorityScore) {
        this.authorityScore = authorityScore;
    }

    /**
     * Returns the locked authority score of this trust family.
     *
     * @return the authority score within {@code [0.40, 1.00]}
     */
    public double authorityScore() {
        return authorityScore;
    }

    /**
     * Returns the locked deterministic ranking of this trust family, where
     * {@code 0} is the most authoritative. The ranking is exactly the
     * authority-score-descending order and is used as the deterministic
     * tie-breaker when two candidates share an authority score.
     *
     * @return the locked rank ({@code 0} = most authoritative)
     */
    public int rank() {
        return switch (this) {
            case OFFICIAL -> 0;
            case ENTERPRISE -> 1;
            case VERIFIED -> 2;
            case COMMUNITY -> 3;
            case UNKNOWN -> 4;
        };
    }

    /**
     * Maps a declared trust label (typically the {@code authority} entry of the
     * registered source metadata) to its trust family.
     *
     * <p>Matching is trimmed, lowercase and <em>exact</em> - no fuzzy matching,
     * no prefix matching, no inference. Anything unrecognized - including
     * {@code null} and blank - maps to {@link #UNKNOWN}. This method therefore
     * never fails.</p>
     *
     * @param label the declared trust label (may be null)
     * @return the matching trust family (never null)
     */
    public static SourceTrustLevel fromLabel(String label) {
        return parse(label).orElse(UNKNOWN);
    }

    /**
     * Parses a declared trust label, distinguishing "declared" from
     * "undeclared".
     *
     * @param label the declared trust label (may be null)
     * @return the matching trust family, or empty when the label is null,
     *         blank or unrecognized (never null)
     */
    public static Optional<SourceTrustLevel> parse(String label) {
        if (label == null || label.isBlank()) {
            return Optional.empty();
        }
        return switch (label.trim().toLowerCase(Locale.ROOT)) {
            case "official" -> Optional.of(OFFICIAL);
            case "enterprise" -> Optional.of(ENTERPRISE);
            case "verified" -> Optional.of(VERIFIED);
            case "community" -> Optional.of(COMMUNITY);
            case "unknown" -> Optional.of(UNKNOWN);
            default -> Optional.empty();
        };
    }

    /**
     * Returns the locked authority score of the given trust family.
     *
     * @param trustLevel the trust family (must not be null)
     * @return the locked authority score within {@code [0.40, 1.00]}
     * @throws NullPointerException if trustLevel is null
     */
    public static double lockedAuthorityScore(SourceTrustLevel trustLevel) {
        Objects.requireNonNull(trustLevel, "trustLevel must not be null");
        return trustLevel.authorityScore;
    }

    @Override
    public String toString() {
        return name() + "(" + String.format(Locale.ROOT, "%.2f", authorityScore) + ")";
    }
}