package platform.kernels.chief.verification;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ChiefVerificationResult</b>
 *
 * <p>Immutable value object representing the result of Chief Kernel verification.
 * This class encapsulates the outcome of architectural verification.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates verification outcome.</li>
 *   <li>Provides immutable verification results.</li>
 *   <li>Contains no verification logic.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final.</li>
 *   <li>Constructor validation — rejects null arguments.</li>
 *   <li>Defensive copying — protects mutable collections.</li>
 *   <li>Value semantics — implements equals, hashCode, toString.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel — Verification Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-107, EIO-ARCH-001</p>
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
public final class ChiefVerificationResult {

    private final boolean architectureValid;
    private final boolean contractsValid;
    private final boolean integrityValid;
    private final List<String> violations;
    private final Map<String, Object> metadata;
    private final Instant verifiedAt;

    /**
     * Constructs a {@code ChiefVerificationResult} with the specified parameters.
     *
     * @param architectureValid whether architecture verification passed
     * @param contractsValid    whether contract verification passed
     * @param integrityValid    whether integrity verification passed
     * @param violations        list of verification violations (must not be {@code null})
     * @param metadata          additional metadata (must not be {@code null})
     * @param verifiedAt        when verification was completed (must not be {@code null})
     * @throws IllegalArgumentException if violations, metadata, or verifiedAt is {@code null}
     */
    public ChiefVerificationResult(
            boolean architectureValid,
            boolean contractsValid,
            boolean integrityValid,
            List<String> violations,
            Map<String, Object> metadata,
            Instant verifiedAt) {
        if (violations == null) {
            throw new IllegalArgumentException("ChiefVerificationResult violations must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("ChiefVerificationResult metadata must not be null");
        }
        if (verifiedAt == null) {
            throw new IllegalArgumentException("ChiefVerificationResult verifiedAt must not be null");
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
     */
    public boolean architectureValid() {
        return architectureValid;
    }

    /**
     * Returns whether contract verification passed.
     *
     * @return {@code true} if contract verification passed
     */
    public boolean contractsValid() {
        return contractsValid;
    }

    /**
     * Returns whether integrity verification passed.
     *
     * @return {@code true} if integrity verification passed
     */
    public boolean integrityValid() {
        return integrityValid;
    }

    /**
     * Returns whether all verifications passed.
     *
     * @return {@code true} if all verifications passed
     */
    public boolean valid() {
        return architectureValid && contractsValid && integrityValid;
    }

    /**
     * Returns an unmodifiable list of verification violations.
     *
     * @return unmodifiable list of violations
     */
    public List<String> violations() {
        return violations;
    }

    /**
     * Returns an unmodifiable view of the metadata.
     *
     * @return unmodifiable map of metadata
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Returns when the verification was completed.
     *
     * @return the verification timestamp
     */
    public Instant verifiedAt() {
        return verifiedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChiefVerificationResult that = (ChiefVerificationResult) obj;
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
        return "ChiefVerificationResult{" +
                "architectureValid=" + architectureValid +
                ", contractsValid=" + contractsValid +
                ", integrityValid=" + integrityValid +
                ", violations=" + violations.size() +
                '}';
    }
}