package com.shreeai.os.platform.kernels.multiagent.verification;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>MultiAgentVerificationResult</b>
 *
 * <p>Immutable value object representing the result of Multi-Agent Kernel verification.
 * This class encapsulates the outcome of architectural, contract, and integrity verification.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Verification Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-MAGENT-107, EIO-ARCH-001</p>
 *
 * <p>MultiAgentVerificationResult is the immutable certification result for the complete
 * Multi-Agent verification pipeline. It aggregates architecture, contract, and integrity
 * findings into a single result.</p>
 *
 * @param architectureValid whether architecture verification passed
 * @param contractsValid    whether contract verification passed
 * @param integrityValid    whether integrity verification passed
 * @param violations        list of verification violations (must not be {@code null})
 * @param metadata          additional metadata (must not be {@code null})
 * @param verifiedAt        when verification was completed (must not be {@code null})
 *
 * @since 1.0
 */
public final class MultiAgentVerificationResult {
    private final boolean architectureValid;
    private final boolean contractsValid;
    private final boolean integrityValid;
    private final List<String> violations;
    private final Map<String, Object> metadata;
    private final Instant verifiedAt;

    /**
     * Constructs a {@code MultiAgentVerificationResult} with the specified parameters.
     *
     * @param architectureValid whether architecture verification passed
     * @param contractsValid    whether contract verification passed
     * @param integrityValid    whether integrity verification passed
     * @param violations        list of verification violations (must not be {@code null})
     * @param metadata          additional metadata (must not be {@code null})
     * @param verifiedAt        when verification was completed (must not be {@code null})
     * @throws IllegalArgumentException if violations, metadata, or verifiedAt is {@code null}
     * @since 1.0
     */
    public MultiAgentVerificationResult(
            boolean architectureValid,
            boolean contractsValid,
            boolean integrityValid,
            List<String> violations,
            Map<String, Object> metadata,
            Instant verifiedAt) {
        if (violations == null) {
            throw new IllegalArgumentException("MultiAgentVerificationResult violations must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("MultiAgentVerificationResult metadata must not be null");
        }
        if (verifiedAt == null) {
            throw new IllegalArgumentException("MultiAgentVerificationResult verifiedAt must not be null");
        }

        this.architectureValid = architectureValid;
        this.contractsValid = contractsValid;
        this.integrityValid = integrityValid;
        this.violations = List.copyOf(violations);
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
        this.verifiedAt = verifiedAt;
    }

    /**
     * Returns whether architecture verification passed.
     *
     * @return {@code true} if architecture verification passed
     * @since 1.0
     */
    public boolean architectureValid() {
        return architectureValid;
    }

    /**
     * Returns whether contract verification passed.
     *
     * @return {@code true} if contract verification passed
     * @since 1.0
     */
    public boolean contractsValid() {
        return contractsValid;
    }

    /**
     * Returns whether integrity verification passed.
     *
     * @return {@code true} if integrity verification passed
     * @since 1.0
     */
    public boolean integrityValid() {
        return integrityValid;
    }

    /**
     * Returns whether all verifications passed.
     *
     * @return {@code true} if all verifications passed
     * @since 1.0
     */
    public boolean valid() {
        return architectureValid && contractsValid && integrityValid;
    }

    /**
     * Returns an unmodifiable list of verification violations.
     *
     * @return unmodifiable list of violations
     * @since 1.0
     */
    public List<String> violations() {
        return violations;
    }

    /**
     * Returns an unmodifiable view of the metadata.
     *
     * @return unmodifiable map of metadata
     * @since 1.0
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Returns when the verification was completed.
     *
     * @return the verification timestamp
     * @since 1.0
     */
    public Instant verifiedAt() {
        return verifiedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MultiAgentVerificationResult that = (MultiAgentVerificationResult) obj;
        return architectureValid == that.architectureValid &&
                contractsValid == that.contractsValid &&
                integrityValid == that.integrityValid &&
                Objects.equals(violations, that.violations) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(verifiedAt, that.verifiedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(architectureValid, contractsValid, integrityValid, violations, metadata, verifiedAt);
    }

    @Override
    public String toString() {
        return "MultiAgentVerificationResult{" +
                "architectureValid=" + architectureValid +
                ", contractsValid=" + contractsValid +
                ", integrityValid=" + integrityValid +
                ", violations=" + violations.size() +
                '}';
    }
}