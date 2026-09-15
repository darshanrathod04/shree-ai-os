package com.shreeai.os.platform.kernels.reasoning.model;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * <b>EvidenceCluster</b>
 *
 * <p>Groups evidence items that discuss the same graph concept. Each cluster
 * is identified by its canonical concept name and carries the deduplicated,
 * stably ordered evidence ids and concept names that contribute to it.</p>
 *
 * <p><b>Deterministic identity:</b> {@code clusterId} is derived as
 * {@code SHA-256(CLUSTER | canonicalConceptName)} and is stable across
 * identical inputs.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R2 Evidence Synthesis</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param clusterId    the deterministic cluster id (never null)
 * @param evidenceIds  the deduplicated, stably ordered evidence chunk ids
 *                     (never null)
 * @param concepts     the deduplicated, canonically sorted concept names
 *                     (never null)
 */
public record EvidenceCluster(
        String clusterId,
        List<String> evidenceIds,
        List<String> concepts) {

    /**
     * Compact constructor that validates, canonically orders and defensively
     * copies all fields.
     *
     * @throws NullPointerException     if any field is null
     * @throws IllegalArgumentException if clusterId is blank
     */
    public EvidenceCluster {
        Objects.requireNonNull(clusterId, "clusterId must not be null");
        Objects.requireNonNull(evidenceIds, "evidenceIds must not be null");
        Objects.requireNonNull(concepts, "concepts must not be null");
        if (clusterId.isBlank()) {
            throw new IllegalArgumentException("clusterId must not be blank");
        }
        evidenceIds = evidenceIds.stream()
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
        concepts = concepts.stream()
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    @Override
    public String toString() {
        return String.format("EvidenceCluster{id=%s, evidence=%d, concepts=%s}",
                clusterId, evidenceIds.size(), concepts);
    }
}
