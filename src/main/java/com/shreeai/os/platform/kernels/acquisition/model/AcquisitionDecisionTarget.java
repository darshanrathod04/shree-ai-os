package com.shreeai.os.platform.kernels.acquisition.model;

import java.util.Objects;

/**
 * <b>AcquisitionDecisionTarget</b>
 *
 * <p>One immutable cache decision binding a required knowledge topic to the
 * selected source that must serve it and to the locked
 * {@link AcquisitionDecision} governing that source.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Carries the topic identity over from {@link SelectedSource} so the
 *       decision plan can be executed and reported per topic.</li>
 *   <li>Names the exact registry source the decision applies to - the
 *       <em>selected</em> source id is preserved even when the registry no
 *       longer holds that source (the decision is then {@code ACQUIRE}).</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This record is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.4 Freshness &amp; Cache Policy</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param topicId   the deterministic topic identifier carried over from the
 *                  selection plan (must not be null or blank)
 * @param topicName the canonical topic name (must not be null or blank)
 * @param sourceId  the deterministic registry id of the selected source (must
 *                  not be null or blank)
 * @param decision  the locked cache decision for this topic (must not be null)
 */
public record AcquisitionDecisionTarget(
        String topicId,
        String topicName,
        String sourceId,
        AcquisitionDecision decision) {

    /**
     * Creates a new AcquisitionDecisionTarget with validation.
     *
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if topicId, topicName or sourceId is blank
     */
    public AcquisitionDecisionTarget {
        Objects.requireNonNull(topicId, "topicId must not be null");
        Objects.requireNonNull(topicName, "topicName must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(decision, "decision must not be null");
        if (topicId.isBlank()) {
            throw new IllegalArgumentException("topicId must not be blank");
        }
        if (topicName.isBlank()) {
            throw new IllegalArgumentException("topicName must not be blank");
        }
        if (sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId must not be blank");
        }
    }

    /**
     * Returns true when this target must be executed by the acquisition
     * execution milestone (K0.6.5): {@code ACQUIRE} or {@code REFRESH}.
     *
     * @return true when fresh knowledge must be obtained for this topic
     */
    public boolean requiresAcquisition() {
        return decision.requiresAcquisition();
    }

    /**
     * Returns a copy of this target carrying the given decision, preserving the
     * topic and source identity. Used only when the locked policy overrides an
     * earlier decision (e.g. an explicit latest request).
     *
     * @param decision the overriding decision (must not be null)
     * @return a new AcquisitionDecisionTarget with the decision replaced (never null)
     */
    public AcquisitionDecisionTarget withDecision(AcquisitionDecision decision) {
        Objects.requireNonNull(decision, "decision must not be null");
        return new AcquisitionDecisionTarget(topicId, topicName, sourceId, decision);
    }

    @Override
    public String toString() {
        return String.format("AcquisitionDecisionTarget{topic='%s', source='%s', decision=%s}",
                topicName, sourceId, decision);
    }
}