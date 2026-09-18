package com.shreeai.os.platform.kernels.acquisition.model;

import java.util.Objects;

/**
 * <b>AcquisitionTarget</b>
 *
 * <p>One immutable routing decision: a single required knowledge topic
 * assigned to exactly one provider type. One topic - one provider.</p>
 *
 * <p><b>Immutability:</b> This record is immutable.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.2 Provider Router</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param topicId   the deterministic topic identifier carried over from the
 *                  {@link KnowledgeTopic} (must not be null or blank)
 * @param topicName the canonical topic name (must not be null or blank)
 * @param provider  the routed provider type (must not be null)
 * @param priority  the requirement priority carried over from the
 *                  {@link KnowledgeTopic} (must not be null)
 */
public record AcquisitionTarget(
        String topicId,
        String topicName,
        ProviderType provider,
        RequirementPriority priority) {

    /**
     * Creates a new AcquisitionTarget with validation.
     *
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if topicId or topicName is blank
     */
    public AcquisitionTarget {
        Objects.requireNonNull(topicId, "topicId must not be null");
        Objects.requireNonNull(topicName, "topicName must not be null");
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(priority, "priority must not be null");
        if (topicId.isBlank()) {
            throw new IllegalArgumentException("topicId must not be blank");
        }
        if (topicName.isBlank()) {
            throw new IllegalArgumentException("topicName must not be blank");
        }
    }

    @Override
    public String toString() {
        return String.format("AcquisitionTarget{topic='%s', provider=%s, priority=%s}",
                topicName, provider, priority);
    }
}
