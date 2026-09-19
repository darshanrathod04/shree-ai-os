package com.shreeai.os.platform.kernels.context.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * <b>DomainProfile</b>
 *
 * <p>Canonical domain intelligence object representing the detected domains of a user request.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Holds the primary detected domain and its confidence.</li>
 *   <li>Provides ranked detected domains for multi-domain scenarios.</li>
 *   <li>Includes detection metadata for auditability.</li>
 *   <li>Single source of truth for domain during pipeline execution.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This record is immutable. All fields are final.</p>
 * <p><b>Thread Safety:</b> Records are inherently thread-safe.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param primaryDomain the detected primary domain (must not be null)
 * @param confidence the confidence score for the primary domain (0.0 to 1.0)
 * @param detectedDomains ranked list of all detected domains, sorted by confidence descending
 * @param detectedAt when the domain was detected (must not be null)
 * @param detectionMethod the method used for detection (must not be null)
 */
public record DomainProfile(
        PrimaryDomain primaryDomain,
        double confidence,
        List<DomainCandidate> detectedDomains,
        Instant detectedAt,
        String detectionMethod
) {
    /**
     * Creates a new DomainProfile with defensive copying and validation.
     *
     * @param primaryDomain the detected primary domain (must not be null)
     * @param confidence the confidence score for the primary domain (0.0 to 1.0)
     * @param detectedDomains ranked list of all detected domains
     * @param detectedAt when the domain was detected (must not be null)
     * @param detectionMethod the method used for detection (must not be null)
     * @return a new DomainProfile instance
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if confidence is out of range
     */
    public static DomainProfile of(PrimaryDomain primaryDomain, double confidence,
                                   List<DomainCandidate> detectedDomains, Instant detectedAt,
                                   String detectionMethod) {
        Objects.requireNonNull(primaryDomain, "primaryDomain must not be null");
        Objects.requireNonNull(detectedAt, "detectedAt must not be null");
        Objects.requireNonNull(detectionMethod, "detectionMethod must not be null");
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException(
                    "confidence must be between 0.0 and 1.0, got: " + confidence);
        }
        List<DomainCandidate> safeDomains = detectedDomains != null
                ? List.copyOf(detectedDomains)
                : List.of();
        return new DomainProfile(primaryDomain, confidence, safeDomains, detectedAt, detectionMethod);
    }

    public PrimaryDomain primaryDomain() {
        return primaryDomain;
    }

    public double confidence() {
        return confidence;
    }

    public List<DomainCandidate> detectedDomains() {
        return detectedDomains;
    }

    public Instant detectedAt() {
        return detectedAt;
    }

    public String detectionMethod() {
        return detectionMethod;
    }

    /**
     * Creates a DomainProfile with UNKNOWN domain and zero confidence.
     *
     * @param detectionMethod the method used for detection
     * @return a DomainProfile representing unknown domain
     */
    public static DomainProfile unknown(String detectionMethod) {
        return new DomainProfile(
                PrimaryDomain.UNKNOWN,
                0.0,
                List.of(),
                Instant.now(),
                detectionMethod
        );
    }

    /**
     * Creates a DomainProfile with GENERAL domain and zero confidence.
     *
     * @param detectionMethod the method used for detection
     * @return a DomainProfile representing general domain
     */
    public static DomainProfile general(String detectionMethod) {
        return new DomainProfile(
                PrimaryDomain.GENERAL,
                0.0,
                List.of(),
                Instant.now(),
                detectionMethod
        );
    }

    @Override
    public String toString() {
        return String.format("DomainProfile{primaryDomain=%s, confidence=%.3f, domains=%d}",
                primaryDomain, confidence, detectedDomains.size());
    }
}