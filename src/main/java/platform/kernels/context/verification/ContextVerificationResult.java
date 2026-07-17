package platform.kernels.context.verification;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ContextVerificationResult</b>
 *
 * <p>An immutable value object representing the result of Context Kernel verification.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates verification outcomes without side effects.</li>
 *   <li>Provides immutable verification metadata for audit and compliance.</li>
 *   <li>Serves as the sole return type for Context verification operations.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This class is immutable. All fields are final
 * and set via constructor. Collections are defensively copied to ensure immutability.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Immutable objects
 * can be safely shared across threads.</p>
 *
 * <p><b>Ownership:</b> Context Kernel - Verification Suite</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-107, EIO-ARCH-001</p>
 *
 * @param successful whether the verification passed
 * @param verifiedAt when the verification was performed
 * @param passedChecks list of passed verification checks (must not be null, will be defensively copied)
 * @param failedChecks list of failed verification checks (must not be null, will be defensively copied)
 * @param metadata additional verification metadata (must not be null, will be defensively copied)
 */
public final class ContextVerificationResult {
    private final boolean successful;
    private final Instant verifiedAt;
    private final List<String> passedChecks;
    private final List<String> failedChecks;
    private final Map<String, Object> metadata;

    /**
     * Creates a new ContextVerificationResult with validation and defensive copying.
     *
     * <p>All parameters are validated for null. Collections are defensively copied
     * to ensure immutability.</p>
     *
     * <p><b>Constructor Validation:</b> Validates all parameters for null and ensures
     * collections are properly initialized.</p>
     *
     * <p><b>Defensive Copying:</b> All collection parameters are copied to prevent
     * external mutation of internal state.</p>
     *
     * @param successful whether the verification passed
     * @param verifiedAt when the verification was performed (must not be null)
     * @param passedChecks list of passed verification checks (must not be null, will be defensively copied)
     * @param failedChecks list of failed verification checks (must not be null, will be defensively copied)
     * @param metadata additional verification metadata (must not be null, will be defensively copied)
     * @throws NullPointerException if verifiedAt, passedChecks, failedChecks, or metadata is null
     */
    public ContextVerificationResult(
            boolean successful,
            Instant verifiedAt,
            List<String> passedChecks,
            List<String> failedChecks,
            Map<String, Object> metadata) {
        Objects.requireNonNull(verifiedAt, "verifiedAt must not be null");
        Objects.requireNonNull(passedChecks, "passedChecks must not be null");
        Objects.requireNonNull(failedChecks, "failedChecks must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");

        this.successful = successful;
        this.verifiedAt = verifiedAt;
        this.passedChecks = Collections.unmodifiableList(List.copyOf(passedChecks));
        this.failedChecks = Collections.unmodifiableList(List.copyOf(failedChecks));
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    /**
     * Returns whether the verification was successful.
     *
     * <p>A verification is successful when all checks pass (failedChecks is empty).</p>
     *
     * @return true if verification passed, false otherwise
     */
    public boolean isSuccessful() {
        return successful;
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
     * Returns an unmodifiable list of passed verification checks.
     *
     * <p>This method ensures that the internal passedChecks list cannot be modified
     * by callers, preserving the immutability contract.</p>
     *
     * @return an unmodifiable list of passed checks (empty if none passed)
     */
    public List<String> getPassedChecks() {
        return passedChecks;
    }

    /**
     * Returns an unmodifiable list of failed verification checks.
     *
     * <p>This method ensures that the internal failedChecks list cannot be modified
     * by callers, preserving the immutability contract.</p>
     *
     * @return an unmodifiable list of failed checks (empty if all passed)
     */
    public List<String> getFailedChecks() {
        return failedChecks;
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
     * <p>Two ContextVerificationResult objects are equal if they have the same
     * successful flag, verifiedAt timestamp, passedChecks, failedChecks, and metadata.</p>
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
        ContextVerificationResult that = (ContextVerificationResult) o;
        return successful == that.successful &&
                Objects.equals(verifiedAt, that.verifiedAt) &&
                Objects.equals(passedChecks, that.passedChecks) &&
                Objects.equals(failedChecks, that.failedChecks) &&
                Objects.equals(metadata, that.metadata);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(successful, verifiedAt, passedChecks, failedChecks, metadata);
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "ContextVerificationResult{" +
                "successful=" + successful +
                ", verifiedAt=" + verifiedAt +
                ", passedChecks=" + passedChecks +
                ", failedChecks=" + failedChecks +
                ", metadata=" + metadata +
                '}';
    }
}