package com.shreeai.os.platform.kernels.acquisition.model;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocument;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>AcquisitionResult</b>
 *
 * <p>The canonical, immutable output artifact of the K0.6.5 Knowledge
 * Acquisition Orchestrator: the knowledge documents produced by executing an
 * {@link AcquisitionDecisionPlan} plus the audit record of every executed
 * decision.</p>
 *
 * <p><b>Canonical form:</b></p>
 * <ul>
 *   <li>{@code documents} holds the authoritative per-source knowledge after
 *       execution - reused cached documents ({@code USE_CACHE}), first-time
 *       ingested documents ({@code ACQUIRE}) and refreshed replacement
 *       documents ({@code REFRESH}); documents are deduplicated by id and
 *       ordered deterministically by source id, then document id.</li>
 *   <li>{@code records} holds one {@link AcquisitionRecord} per executed
 *       decision target, ordered deterministically by decision priority
 *       (ACQUIRE, REFRESH, USE_CACHE), then topic name, then source id.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This record is deeply immutable - both lists are
 * defensively copied and unmodifiable, and every contained element is
 * immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.5 Acquisition Orchestrator</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param documents the authoritative documents after execution, in canonical
 *                  order (never null)
 * @param records   the stably-ordered execution audit records (never null)
 */
public record AcquisitionResult(
        List<KnowledgeDocument> documents,
        List<AcquisitionRecord> records) {

    /** Deterministic document ordering: source id, then document id. */
    private static final Comparator<KnowledgeDocument> DOCUMENT_ORDER =
            Comparator.comparing(KnowledgeDocument::sourceId)
                    .thenComparing(KnowledgeDocument::documentId);

    /**
     * Creates a deeply-immutable AcquisitionResult with defensive copying.
     *
     * @throws NullPointerException if documents or records is null
     */
    public AcquisitionResult {
        Objects.requireNonNull(documents, "documents must not be null");
        Objects.requireNonNull(records, "records must not be null");
        documents = List.copyOf(documents);
        records = List.copyOf(records);
    }

    /**
     * Builds a canonical result from execution output: documents are
     * deduplicated by id (keeping the first occurrence in iteration order) and
     * sorted deterministically; records are passed through unchanged.
     *
     * @param executedDocuments the documents produced during execution, in
     *                          execution order (must not be null)
     * @param executedRecords   the audit records in their final stable order
     *                          (must not be null)
     * @return a canonical, immutable AcquisitionResult (never null)
     */
    public static AcquisitionResult of(List<KnowledgeDocument> executedDocuments,
                                       List<AcquisitionRecord> executedRecords) {
        Objects.requireNonNull(executedDocuments, "executedDocuments must not be null");
        Objects.requireNonNull(executedRecords, "executedRecords must not be null");
        Map<String, KnowledgeDocument> unique = new LinkedHashMap<>();
        for (KnowledgeDocument document : executedDocuments) {
            unique.putIfAbsent(document.documentId(), document);
        }
        List<KnowledgeDocument> ordered = unique.values().stream()
                .sorted(DOCUMENT_ORDER)
                .toList();
        return new AcquisitionResult(ordered, executedRecords);
    }

    /**
     * Returns an empty acquisition result (no documents, no records).
     *
     * @return an empty AcquisitionResult (never null)
     */
    public static AcquisitionResult empty() {
        return new AcquisitionResult(List.of(), List.of());
    }

    /**
     * Returns the number of executed decisions in this result.
     *
     * @return the record count
     */
    public int size() {
        return records.size();
    }

    /**
     * Returns the number of successfully acquired or refreshed documents.
     *
     * @return the acquired count (never negative)
     */
    public long acquiredCount() {
        return records.stream().filter(record -> record.status() == AcquisitionStatus.ACQUIRED).count();
    }

    /**
     * Returns the number of decisions satisfied from cache.
     *
     * @return the skipped count (never negative)
     */
    public long skippedCount() {
        return records.stream().filter(record -> record.status() == AcquisitionStatus.SKIPPED).count();
    }

    /**
     * Returns the number of decisions that could not be executed.
     *
     * @return the failed count (never negative)
     */
    public long failedCount() {
        return records.stream().filter(record -> record.status() == AcquisitionStatus.FAILED).count();
    }

    @Override
    public String toString() {
        return "AcquisitionResult{documents=" + documents.size()
                + ", records=" + records.size() + "}";
    }
}
