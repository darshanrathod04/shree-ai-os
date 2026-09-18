package com.shreeai.os.platform.kernels.acquisition.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * <b>AcquisitionDecisionPlan</b>
 *
 * <p>The canonical, immutable output artifact of the K0.6.4 Freshness &amp;
 * Cache Policy Engine: a deterministic decision, for every selected source,
 * between reusing cached knowledge and acquiring fresh knowledge.</p>
 *
 * <p>This artifact answers exactly one question and nothing more - <em>should
 * Shree AI OS use cached knowledge or acquire fresh knowledge?</em> It contains
 * no downloaded content, no crawled pages, no parsed documents and no execution
 * instructions; K0.6.5 executes the decisions, this milestone only makes
 * them.</p>
 *
 * <p><b>Canonical form:</b> decisions follow the canonical order of the
 * {@link SourceSelectionPlan} they were derived from, so the plan is stable and
 * reproducible. The reason list is ordered identically to the target list -
 * position {@code i} of the reasons explains position {@code i} of the
 * targets.</p>
 *
 * <p><b>Immutability:</b> This record is deeply immutable - both lists are
 * defensively copied and unmodifiable, and every contained
 * {@link AcquisitionDecisionTarget} and {@link FreshnessReason} is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.4 Freshness &amp; Cache Policy</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param targets the stably-ordered cache decisions, one per selected source
 *                (never null)
 * @param reasons the stably-ordered justifications, aligned by position with
 *                {@code targets} (never null)
 */
public record AcquisitionDecisionPlan(
        List<AcquisitionDecisionTarget> targets,
        List<FreshnessReason> reasons) {

    /**
     * Creates a deeply-immutable AcquisitionDecisionPlan with defensive copying.
     *
     * @throws NullPointerException if targets or reasons is null
     */
    public AcquisitionDecisionPlan {
        Objects.requireNonNull(targets, "targets must not be null");
        Objects.requireNonNull(reasons, "reasons must not be null");
        targets = List.copyOf(targets);
        reasons = List.copyOf(reasons);
    }

    /**
     * Returns an empty decision plan (no decisions, no reasons).
     *
     * @return an empty AcquisitionDecisionPlan (never null)
     */
    public static AcquisitionDecisionPlan empty() {
        return new AcquisitionDecisionPlan(List.of(), List.of());
    }

    /**
     * Returns the number of decisions in this plan.
     *
     * @return the decision count
     */
    public int size() {
        return targets.size();
    }

    /**
     * Looks up the decision made for a specific topic id.
     *
     * @param topicId the deterministic topic identifier
     * @return the matching decision, or empty when the topic has no decision
     *         (never null)
     */
    public Optional<AcquisitionDecisionTarget> decisionFor(String topicId) {
        if (topicId == null) {
            return Optional.empty();
        }
        return targets.stream()
                .filter(target -> target.topicId().equals(topicId))
                .findFirst();
    }

    /**
     * Looks up the justification recorded for a specific source id.
     *
     * @param sourceId the deterministic registry source id
     * @return the matching reason, or empty when no reason is recorded (never null)
     */
    public Optional<FreshnessReason> reasonFor(String sourceId) {
        if (sourceId == null) {
            return Optional.empty();
        }
        return reasons.stream()
                .filter(reason -> reason.sourceId().equals(sourceId))
                .findFirst();
    }

    /**
     * Returns the targets whose decision requires knowledge to be obtained -
     * {@code ACQUIRE} or {@code REFRESH} - in canonical plan order. These are
     * exactly the targets executed by K0.6.5.
     *
     * @return an immutable list of targets requiring acquisition (never null)
     */
    public List<AcquisitionDecisionTarget> targetsRequiringAcquisition() {
        return targets.stream()
                .filter(AcquisitionDecisionTarget::requiresAcquisition)
                .toList();
    }

    /**
     * Returns the targets whose knowledge may be reused from cache, in
     * canonical plan order.
     *
     * @return an immutable list of cache-reusing targets (never null)
     */
    public List<AcquisitionDecisionTarget> cachedTargets() {
        return targets.stream()
                .filter(target -> target.decision().usesCache())
                .toList();
    }

    /**
     * Returns the decided topic names in canonical plan order.
     *
     * @return an immutable list of topic names (never null)
     */
    public List<String> decidedTopicNames() {
        return targets.stream().map(AcquisitionDecisionTarget::topicName).toList();
    }

    @Override
    public String toString() {
        return "AcquisitionDecisionPlan{targets=" + targets.size()
                + ", reasons=" + reasons.size() + "}";
    }
}