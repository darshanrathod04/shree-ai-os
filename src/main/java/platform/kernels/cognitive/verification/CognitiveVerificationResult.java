package platform.kernels.cognitive.verification;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>CognitiveVerificationResult</b>
 *
 * <p>Immutable value object representing the result of a cognitive architecture
 * verification process.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates verification findings in an immutable structure.</li>
 *   <li>Provides deterministic verification outcome.</li>
 *   <li>Preserves verification metadata for audit purposes.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final and set at construction.</li>
 *   <li>Defensive copying — collections are unmodifiable.</li>
 *   <li>Thread-safe — no mutable state.</li>
 *   <li>Value-based equality — equals() and hashCode() implemented.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-107, EIO-ARCH-001</p>
 *
 * @param successful    whether the verification passed (true) or failed (false)
 * @param findings      list of verification findings (must not be null, may be empty)
 * @param verifiedAt     timestamp of when verification was performed (must not be null)
 * @param metadata      additional verification metadata (must not be null)
 */
public final class CognitiveVerificationResult {

    private final boolean successful;
    private final List<String> findings;
    private final Instant verifiedAt;
    private final Map<String, Object> metadata;

    /**
     * Creates a new CognitiveVerificationResult with the specified parameters.
     *
     * <p>Performs defensive validation and copying to ensure immutability.</p>
     *
     * @param successful whether the verification passed
     * @param findings   list of verification findings (must not be null)
     * @param verifiedAt  timestamp of verification (must not be null)
     * @param metadata   additional metadata (must not be null)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public CognitiveVerificationResult(boolean successful,
                                       List<String> findings,
                                       Instant verifiedAt,
                                       Map<String, Object> metadata) {
        if (findings == null) {
            throw new IllegalArgumentException("Findings must not be null");
        }
        if (verifiedAt == null) {
            throw new IllegalArgumentException("VerifiedAt must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata must not be null");
        }

        this.successful = successful;
        this.findings = Collections.unmodifiableList(List.copyOf(findings));
        this.verifiedAt = verifiedAt;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    /**
     * Returns whether the verification was successful.
     *
     * @return true if verification passed, false otherwise
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Returns the unmodifiable list of verification findings.
     *
     * <p>Each finding describes a specific architectural compliance issue or
     * confirmation.</p>
     *
     * @return unmodifiable list of findings (never null, may be empty)
     */
    public List<String> getFindings() {
        return findings;
    }

    /**
     * Returns the timestamp when verification was performed.
     *
     * @return the verification timestamp (never null)
     */
    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    /**
     * Returns the unmodifiable map of verification metadata.
     *
     * <p>Metadata may include verifier names, execution times, or other
     * audit information.</p>
     *
     * @return unmodifiable map of metadata (never null)
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is equal to this one.
     *
     * <p>Equality is based on all fields: successful, findings, verifiedAt,
     * and metadata.</p>
     *
     * @param obj the reference object with which to compare
     * @return true if this object is equal to the obj argument
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        CognitiveVerificationResult that = (CognitiveVerificationResult) obj;
        return successful == that.successful &&
               findings.equals(that.findings) &&
               verifiedAt.equals(that.verifiedAt) &&
               metadata.equals(that.metadata);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        int result = Boolean.hashCode(successful);
        result = 31 * result + findings.hashCode();
        result = 31 * result + verifiedAt.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    /**
     * Returns a string representation of the verification result.
     *
     * @return a string describing the verification result
     */
    @Override
    public String toString() {
        return "CognitiveVerificationResult{" +
               "successful=" + successful +
               ", findings=" + findings +
               ", verifiedAt=" + verifiedAt +
               ", metadata=" + metadata +
               '}';
    }
}