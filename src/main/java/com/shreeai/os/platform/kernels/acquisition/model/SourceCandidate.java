package com.shreeai.os.platform.kernels.acquisition.model;

import java.util.Locale;
import java.util.Objects;

/**
 * <b>SourceCandidate</b>
 *
 * <p>An immutable, trust-annotated view of one concrete knowledge source that
 * is eligible to serve a required knowledge topic through a specific
 * {@link ProviderType}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Materializes one registered {@code KnowledgeSource} as a ranking input
 *       for the K0.6.3 Trust &amp; Source Selection Engine.</li>
 *   <li>Carries the source identity, the provider under which it is being
 *       considered, its trust family and its locked authority score.</li>
 * </ul>
 *
 * <p><b>Locked authority policy:</b> the {@code authorityScore} of a candidate
 * is always derived from its {@link SourceTrustLevel} via
 * {@link #forTrust(String, String, ProviderType, SourceTrustLevel)} - the
 * platform never invents, weights or normalizes an authority score.</p>
 *
 * <p><b>Immutability:</b> This record is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.3 Trust &amp; Source Selection</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param sourceId       the deterministic registry source id (must not be null
 *                       or blank)
 * @param sourceName     the human-readable source name (must not be null or
 *                       blank)
 * @param provider       the provider type under which this source is being
 *                       considered (must not be null)
 * @param trustLevel     the declared trust family (must not be null)
 * @param authorityScore the authority score within {@code [0.0, 1.0]}, derived
 *                       from the locked trust policy
 */
public record SourceCandidate(
        String sourceId,
        String sourceName,
        ProviderType provider,
        SourceTrustLevel trustLevel,
        double authorityScore) {

    /**
     * Creates a new SourceCandidate with validation.
     *
     * @throws NullPointerException     if any reference parameter is null
     * @throws IllegalArgumentException if ids are blank or the authority score
     *                                  is outside {@code [0.0, 1.0]}
     */
    public SourceCandidate {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(sourceName, "sourceName must not be null");
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(trustLevel, "trustLevel must not be null");
        if (sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId must not be blank");
        }
        if (sourceName.isBlank()) {
            throw new IllegalArgumentException("sourceName must not be blank");
        }
        if (authorityScore < 0.0 || authorityScore > 1.0) {
            throw new IllegalArgumentException(
                    "authorityScore must be within [0.0, 1.0]: " + authorityScore);
        }
    }

    /**
     * Creates a candidate whose authority score is the <em>locked</em> score of
     * its trust family. This is the single construction path used by the
     * selection engine.
     *
     * @param sourceId   the deterministic registry source id (must not be null)
     * @param sourceName the human-readable source name (must not be null)
     * @param provider   the provider type under consideration (must not be null)
     * @param trustLevel the declared trust family (must not be null)
     * @return a new immutable SourceCandidate carrying the locked authority
     *         score (never null)
     */
    public static SourceCandidate forTrust(String sourceId,
                                           String sourceName,
                                           ProviderType provider,
                                           SourceTrustLevel trustLevel) {
        Objects.requireNonNull(trustLevel, "trustLevel must not be null");
        return new SourceCandidate(sourceId, sourceName, provider, trustLevel,
                trustLevel.authorityScore());
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "SourceCandidate{source='%s', provider=%s, trust=%s, authority=%.2f}",
                sourceName, provider, trustLevel, authorityScore);
    }
}