package com.shreeai.os.platform.kernels.acquisition.model;

import java.util.Objects;

/**
 * <b>AcquisitionRecord</b>
 *
 * <p>One immutable execution record of the K0.6.5 Knowledge Acquisition
 * Orchestrator: the outcome of executing exactly one
 * {@link AcquisitionDecisionTarget} of a {@link AcquisitionDecisionPlan}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Audits what happened to every acquisition decision - acquired,
 *       reused, or failed - without exposing ingestion internals.</li>
 *   <li>Names the document that now represents the topic's knowledge when one
 *       exists ({@code documentId} is nullable: a {@code FAILED} record carries
 *       none).</li>
 * </ul>
 *
 * <p><b>Determinism:</b> records carry no timestamps - the execution order is
 * derived deterministically by the orchestrator (decision priority, topic
 * name, source id), never from wall-clock data.</p>
 *
 * <p><b>Immutability:</b> This record is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.5 Acquisition Orchestrator</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param topicId    the deterministic topic identifier carried over from the
 *                   decision target (must not be null or blank)
 * @param sourceId   the deterministic registry id of the executed source (must
 *                   not be null or blank)
 * @param decision   the executed cache decision (must not be null)
 * @param status     the execution outcome (must not be null)
 * @param documentId the deterministic id of the document now representing the
 *                   topic's knowledge; {@code null} when no document exists
 *                   (for example a {@code FAILED} record)
 */
public record AcquisitionRecord(
        String topicId,
        String sourceId,
        AcquisitionDecision decision,
        AcquisitionStatus status,
        String documentId) {

    /**
     * Creates a new AcquisitionRecord with validation. The {@code documentId}
     * is deliberately nullable.
     *
     * @throws NullPointerException     if topicId, sourceId, decision or status
     *                                  is null
     * @throws IllegalArgumentException if topicId or sourceId is blank
     */
    public AcquisitionRecord {
        Objects.requireNonNull(topicId, "topicId must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(decision, "decision must not be null");
        Objects.requireNonNull(status, "status must not be null");
        if (topicId.isBlank()) {
            throw new IllegalArgumentException("topicId must not be blank");
        }
        if (sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId must not be blank");
        }
    }

    /**
     * Returns a copy of this record carrying the given document id, preserving
     * every other field.
     *
     * @param documentId the document id (may be null)
     * @return a new AcquisitionRecord with the document id set (never null)
     */
    public AcquisitionRecord withDocumentId(String documentId) {
        return new AcquisitionRecord(topicId, sourceId, decision, status, documentId);
    }

    @Override
    public String toString() {
        return String.format("AcquisitionRecord{topic='%s', source='%s', decision=%s, status=%s, documentId=%s}",
                topicId, sourceId, decision, status, documentId);
    }
}
