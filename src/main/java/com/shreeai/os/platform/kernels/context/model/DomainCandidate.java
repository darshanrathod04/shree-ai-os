package com.shreeai.os.platform.kernels.context.model;

import java.util.Objects;

/**
 * <b>DomainCandidate</b>
 *
 * <p>Represents a ranked detected domain from user input.</p>
 *
 * <p><b>Immutability:</b> This record is immutable. All fields are final.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param domain the primary domain candidate (must not be null)
 * @param confidence the confidence score (0.0 to 1.0)
 */
public record DomainCandidate(
        PrimaryDomain domain,
        double confidence
) {
    /**
     * Creates a new DomainCandidate with validation.
     *
     * @param domain the primary domain candidate (must not be null)
     * @param confidence the confidence score (0.0 to 1.0)
     * @return a new DomainCandidate instance
     * @throws NullPointerException if domain is null
     * @throws IllegalArgumentException if confidence is out of range
     */
    public static DomainCandidate of(PrimaryDomain domain, double confidence) {
        Objects.requireNonNull(domain, "domain must not be null");
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException(
                    "confidence must be between 0.0 and 1.0, got: " + confidence);
        }
        return new DomainCandidate(domain, confidence);
    }

    public PrimaryDomain domain() {
        return domain;
    }

    public double confidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return String.format("DomainCandidate{domain=%s, confidence=%.3f}", domain, confidence);
    }
}