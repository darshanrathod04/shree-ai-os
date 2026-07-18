package platform.kernels.knowledge.verification;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>KnowledgeVerificationResult</b>
 *
 * <p>An immutable value object representing the result of a Knowledge Kernel
 * architectural verification.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the outcome of architectural certification.</li>
 *   <li>Carries verification findings and metadata.</li>
 *   <li>Serves as the sole return type for all verification operations.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This class is immutable. All fields are final
 * and set via constructor. Collections are defensively copied to ensure immutability.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Immutable objects
 * can be safely shared across threads.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-107, EIO-ARCH-001</p>
 */
public final class KnowledgeVerificationResult {

    private final boolean successful;
    private final List<String> findings;
    private final Instant verifiedAt;
    private final Map<String, Object> metadata;

    private KnowledgeVerificationResult(
            boolean successful,
            List<String> findings,
            Instant verifiedAt,
            Map<String, Object> metadata) {
        this.successful = successful;
        this.findings = findings;
        this.verifiedAt = verifiedAt;
        this.metadata = metadata;
    }

    /**
     * Creates a new KnowledgeVerificationResult.
     *
     * <p>All parameters are validated for null. The findings list and metadata map
     * are defensively copied to ensure immutability.</p>
     *
     * @param successful  whether the verification was successful
     * @param findings    list of verification findings (must not be null, will be defensively copied)
     * @param verifiedAt  when the verification was performed (must not be null)
     * @param metadata    additional verification metadata (must not be null, will be defensively copied)
     * @return a new KnowledgeVerificationResult instance
     * @throws NullPointerException if findings, verifiedAt, or metadata is null
     */
    public static KnowledgeVerificationResult of(
            boolean successful,
            List<String> findings,
            Instant verifiedAt,
            Map<String, Object> metadata) {
        Objects.requireNonNull(findings, "findings must not be null");
        Objects.requireNonNull(verifiedAt, "verifiedAt must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");

        List<String> unmodifiableFindings = Collections.unmodifiableList(new java.util.ArrayList<>(findings));
        Map<String, Object> unmodifiableMetadata = Collections.unmodifiableMap(new HashMap<>(metadata));

        return new KnowledgeVerificationResult(successful, unmodifiableFindings, verifiedAt, unmodifiableMetadata);
    }

    /**
     * Returns whether the verification was successful.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Returns an unmodifiable list of verification findings.
     *
     * <p>This method ensures that the internal findings list cannot be modified
     * by callers, preserving the immutability contract.</p>
     *
     * @return an unmodifiable list of findings
     */
    public List<String> getFindings() {
        return findings;
    }

    /**
     * Returns when the verification was performed.
     *
     * @return the verification timestamp
     */
    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    /**
     * Returns an unmodifiable map of verification metadata.
     *
     * <p>This method ensures that the internal metadata map cannot be modified
     * by callers, preserving the immutability contract.</p>
     *
     * @return an unmodifiable map of metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two KnowledgeVerificationResult objects are equal if they have the same
     * successful flag, findings, verifiedAt timestamp, and metadata.</p>
     *
     * @param o the reference object with which to compare
     * @return true if this object is the same as the obj argument; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KnowledgeVerificationResult that = (KnowledgeVerificationResult) o;
        return successful == that.successful
                && Objects.equals(findings, that.findings)
                && Objects.equals(verifiedAt, that.verifiedAt)
                && Objects.equals(metadata, that.metadata);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(successful, findings, verifiedAt, metadata);
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "KnowledgeVerificationResult{" +
                "successful=" + successful +
                ", findings=" + findings +
                ", verifiedAt=" + verifiedAt +
                ", metadata=" + metadata +
                '}';
    }
}