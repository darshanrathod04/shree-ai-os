package com.shreeai.os.platform.intelligence.evidence;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>EvidenceProvenance</b>
 *
 * <p>Records the origin of an {@link EvidenceItem} so the system can always answer
 * "Where did this information come from?"</p>
 *
 * <p>The provenance model supports future reasoning verification by preserving the
 * source type, source identifier, source location, capture time, and extraction
 * method for every meaningful information item entering the intelligence pipeline.</p>
 *
 * <p><b>Ownership:</b> Intelligence Foundation</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param sourceType the type of the origin source (e.g. FILE, PROJECT, LOG, MEMORY, KNOWLEDGE, SYSTEM)
 * @param sourceId the identifier of the origin source (e.g. file path, project id, memory id)
 * @param sourceLocation the location within the source, such as a line range or package
 *                       (may be {@code null} when not applicable)
 * @param capturedAt the timestamp when the evidence was captured from the source
 * @param extractionMethod the method used to extract this evidence (may be {@code null})
 * @param parentEvidenceId the identifier of a parent evidence item that this item was
 *                         derived from (may be {@code null} for root evidence)
 */
public record EvidenceProvenance(
        String sourceType,
        String sourceId,
        String sourceLocation,
        Instant capturedAt,
        String extractionMethod,
        String parentEvidenceId
) {

    /**
     * Creates a new EvidenceProvenance with validation.
     *
     * @param sourceType the type of the origin source (must not be null or blank)
     * @param sourceId the identifier of the origin source (must not be null or blank)
     * @param sourceLocation the location within the source (may be null)
     * @param capturedAt the capture timestamp (must not be null)
     * @param extractionMethod the extraction method (may be null)
     * @param parentEvidenceId the parent evidence identifier (may be null)
     * @throws NullPointerException if sourceType, sourceId, or capturedAt is null
     * @throws IllegalArgumentException if sourceType or sourceId is blank
     */
    public EvidenceProvenance {
        Objects.requireNonNull(sourceType, "sourceType must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(capturedAt, "capturedAt must not be null");
        if (sourceType.isBlank()) {
            throw new IllegalArgumentException("sourceType must not be blank");
        }
        if (sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId must not be blank");
        }
    }

    /**
     * Creates a simple provenance record with only the required fields.
     *
     * @param sourceType the type of the origin source
     * @param sourceId the identifier of the origin source
     * @param capturedAt the capture timestamp
     * @return a new EvidenceProvenance
     */
    public static EvidenceProvenance of(String sourceType, String sourceId, Instant capturedAt) {
        return new EvidenceProvenance(sourceType, sourceId, null, capturedAt, null, null);
    }
}