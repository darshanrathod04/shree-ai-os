package com.shreeai.os.platform.kernels.acquisition.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>KnowledgeRequirementSet</b>
 *
 * <p>The canonical, immutable output artifact of the K0.6.1 Source Discovery
 * Engine: a structured Knowledge Requirement Set describing <em>what knowledge
 * is required to solve the request</em>.</p>
 *
 * <p>This artifact answers exactly one question and nothing more. It contains
 * no documents, no providers, no sources and no acquisition instructions -
 * those belong to K0.6.2+ (Provider Router and beyond).</p>
 *
 * <p><b>Canonical form:</b> topics are deduplicated, canonically ordered
 * (priority descending CRITICAL&rarr;LOW, then topic name ascending) and carry
 * deterministic SHA-256 identifiers. Reasons are preserved in discovery order.
 * The set is deeply immutable.</p>
 *
 * <p><b>Immutability:</b> This record is deeply immutable - both lists are
 * defensively copied and unmodifiable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.1 Source Discovery</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param topics  the canonically-ordered discovered knowledge topics (never null)
 * @param reasons the discovery reasons in discovery order (never null)
 */
public record KnowledgeRequirementSet(
        List<KnowledgeTopic> topics,
        List<DiscoveryReason> reasons) {

    /**
     * Creates a deeply-immutable KnowledgeRequirementSet with defensive copies.
     *
     * @throws NullPointerException if topics or reasons is null
     */
    public KnowledgeRequirementSet {
        Objects.requireNonNull(topics, "topics must not be null");
        Objects.requireNonNull(reasons, "reasons must not be null");
        topics = List.copyOf(topics);
        reasons = List.copyOf(reasons);
    }

    /**
     * Returns an empty requirement set (no topics, no reasons).
     *
     * @return an empty KnowledgeRequirementSet (never null)
     */
    public static KnowledgeRequirementSet empty() {
        return new KnowledgeRequirementSet(List.of(), List.of());
    }

    @Override
    public String toString() {
        return "KnowledgeRequirementSet{topics=" + topics.size()
                + ", reasons=" + reasons.size() + "}";
    }
}
