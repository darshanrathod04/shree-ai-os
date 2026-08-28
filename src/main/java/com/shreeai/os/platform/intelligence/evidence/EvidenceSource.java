package com.shreeai.os.platform.intelligence.evidence;

import java.util.Objects;

/**
 * <b>EvidenceSource</b>
 *
 * <p>Identifies the origin source of an {@link EvidenceItem}.</p>
 *
 * <p>The source type aligns with {@link EvidenceType} categories so a FILE evidence
 * item originates from a FILE source, a MEMORY evidence item originates from a MEMORY
 * source, and so on. The source identifier is the stable reference used to trace the
 * origin (file path, project id, memory id, knowledge id, etc.).</p>
 *
 * <p><b>Ownership:</b> Intelligence Foundation</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param sourceType the source category (must not be null)
 * @param sourceId the stable identifier of the source (must not be null or blank)
 * @param displayName a human-readable name for the source (may be {@code null})
 */
public record EvidenceSource(
        EvidenceType sourceType,
        String sourceId,
        String displayName
) {

    /**
     * Creates a new EvidenceSource with validation.
     *
     * @throws NullPointerException if sourceType or sourceId is null
     * @throws IllegalArgumentException if sourceId is blank
     */
    public EvidenceSource {
        Objects.requireNonNull(sourceType, "sourceType must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        if (sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId must not be blank");
        }
    }

    /**
     * Creates a simple EvidenceSource with default display name.
     *
     * @param sourceType the source category
     * @param sourceId the stable identifier of the source
     * @return a new EvidenceSource
     */
    public static EvidenceSource of(EvidenceType sourceType, String sourceId) {
        return new EvidenceSource(sourceType, sourceId, null);
    }
}