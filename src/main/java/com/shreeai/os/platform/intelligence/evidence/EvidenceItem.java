package com.shreeai.os.platform.intelligence.evidence;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>EvidenceItem</b>
 *
 * <p>First-class evidence representation for the Shree AI OS intelligence pipeline.</p>
 *
 * <p>An evidence item is any meaningful piece of information that enters the
 * intelligence flow, with an explicit type, source, provenance, confidence, and
 * timestamp. Evidence items are immutable and are designed to coexist even when
 * they contradict one another — the system never silently overwrites or discards
 * conflicting evidence.</p>
 *
 * <p><b>Ownership:</b> Intelligence Foundation</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param id the unique evidence identifier (must not be null or blank)
 * @param type the evidence classification (must not be null)
 * @param value the content of the evidence (must not be null)
 * @param source the originating source reference (must not be null)
 * @param provenance the origin trace for this evidence (must not be null)
 * @param confidence the confidence in this evidence, 0.0–1.0 (must be within range)
 * @param capturedAt when this evidence was captured
 * @param relevanceScore preliminary relevance score 0.0–1.0 used by later retrieval
 *                       phases (may be {@code null} until relevance is computed)
 * @param sourceReliability reliability of the source 0.0–1.0 (may be {@code null})
 * @param importance importance weight 0.0–1.0 (may be {@code null})
 * @param scope the scope this evidence applies to, e.g. "project-global",
 *              "file:src/App.java", "memory:episodic" (may be {@code null})
 * @param relationships identifiers of related evidence items (defensively copied)
 * @param metadata additional metadata (defensively copied)
 */
public record EvidenceItem(
        String id,
        EvidenceType type,
        String value,
        EvidenceSource source,
        EvidenceProvenance provenance,
        double confidence,
        Instant capturedAt,
        Double relevanceScore,
        Double sourceReliability,
        Double importance,
        String scope,
        List<String> relationships,
        Map<String, Object> metadata
) {

    /**
     * Creates a new EvidenceItem with full validation.
     *
     * @throws NullPointerException if id, type, value, source, provenance, or capturedAt is null
     * @throws IllegalArgumentException if id is blank or confidence is outside [0.0, 1.0]
     */
    public EvidenceItem {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(provenance, "provenance must not be null");
        Objects.requireNonNull(capturedAt, "capturedAt must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
        }
        if (relevanceScore != null && (relevanceScore < 0.0 || relevanceScore > 1.0)) {
            throw new IllegalArgumentException("relevanceScore must be between 0.0 and 1.0");
        }
        if (sourceReliability != null && (sourceReliability < 0.0 || sourceReliability > 1.0)) {
            throw new IllegalArgumentException("sourceReliability must be between 0.0 and 1.0");
        }
        if (importance != null && (importance < 0.0 || importance > 1.0)) {
            throw new IllegalArgumentException("importance must be between 0.0 and 1.0");
        }
        relationships = relationships != null ? List.copyOf(relationships) : List.of();
        metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    /**
     * Convenience factory for a simple evidence item with default provenance.
     *
     * @param id the evidence identifier
     * @param type the evidence type
     * @param value the evidence content
     * @param source the source of this evidence
     * @param confidence the confidence 0.0–1.0
     * @param capturedAt the capture time
     * @return a new EvidenceItem
     */
    public static EvidenceItem of(
            String id,
            EvidenceType type,
            String value,
            EvidenceSource source,
            double confidence,
            Instant capturedAt) {
        return new EvidenceItem(
                id,
                type,
                value,
                source,
                EvidenceProvenance.of(source.sourceType().name(), source.sourceId(), capturedAt),
                confidence,
                capturedAt,
                null,
                null,
                null,
                null,
                List.of(),
                Map.of()
        );
    }
}