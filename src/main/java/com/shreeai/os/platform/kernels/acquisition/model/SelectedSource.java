package com.shreeai.os.platform.kernels.acquisition.model;

import java.util.Objects;

/**
 * <b>SelectedSource</b>
 *
 * <p>One immutable selection decision: a single required knowledge topic bound
 * to exactly one concrete, trusted source. One topic - one selected source.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Binds a canonical topic (identity carried over from
 *       {@link KnowledgeTopic} / {@link AcquisitionTarget}) to the winning
 *       {@link SourceCandidate}.</li>
 *   <li>Provides enterprise-grade provenance: the selection names the exact
 *       registry source that must be acquired, together with its trust family
 *       and locked authority score.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This record is immutable - the contained candidate is
 * itself immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.3 Trust &amp; Source Selection</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param topicId   the deterministic topic identifier carried over from the
 *                  acquisition target (must not be null or blank)
 * @param topicName the canonical topic name (must not be null or blank)
 * @param source    the winning source candidate (must not be null)
 */
public record SelectedSource(
        String topicId,
        String topicName,
        SourceCandidate source) {

    /**
     * Creates a new SelectedSource with validation.
     *
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if topicId or topicName is blank
     */
    public SelectedSource {
        Objects.requireNonNull(topicId, "topicId must not be null");
        Objects.requireNonNull(topicName, "topicName must not be null");
        Objects.requireNonNull(source, "source must not be null");
        if (topicId.isBlank()) {
            throw new IllegalArgumentException("topicId must not be blank");
        }
        if (topicName.isBlank()) {
            throw new IllegalArgumentException("topicName must not be blank");
        }
    }

    /**
     * Returns the deterministic registry id of the selected source.
     *
     * @return the selected source id (never null)
     */
    public String sourceId() {
        return source.sourceId();
    }

    /**
     * Returns the name of the selected source.
     *
     * @return the selected source name (never null)
     */
    public String sourceName() {
        return source.sourceName();
    }

    /**
     * Returns the locked authority score of the selected source.
     *
     * @return the authority score within {@code [0.0, 1.0]}
     */
    public double authorityScore() {
        return source.authorityScore();
    }

    @Override
    public String toString() {
        return String.format("SelectedSource{topic='%s', source='%s', trust=%s}",
                topicName, source.sourceName(), source.trustLevel());
    }
}