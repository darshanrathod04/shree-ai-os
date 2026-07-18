package platform.kernels.identity.verification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>IdentityVerificationResult</b>
 *
 * <p>An immutable value object representing the result of an Identity verification operation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the outcome of verification operations.</li>
 *   <li>Provides lists of passed and failed checks.</li>
 *   <li>Never exposes mutable state or collections.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> This class is immutable. All fields are final and
 * initialized via constructor. All collections are defensively copied and
 * exposed as unmodifiable.</p>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe. Immutable objects
 * can be safely shared across threads.</p>
 *
 * <p><b>Ownership:</b> Identity Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-ID-107</p>
 *
 * @param successful whether all verification checks passed
 * @param verifiedAt when the verification was performed
 * @param passedChecks list of passed verification checks (must not be null)
 * @param failedChecks list of failed verification checks (must not be null)
 * @param metadata additional verification metadata (must not be null)
 */
public final class IdentityVerificationResult {
    private final boolean successful;
    private final Instant verifiedAt;
    private final List<String> passedChecks;
    private final List<String> failedChecks;
    private final Map<String, Object> metadata;

    /**
     * Creates a new IdentityVerificationResult with the specified parameters.
     *
     * <p>All parameters are validated for null. The passedChecks and failedChecks
     * lists are copied to ensure immutability. The metadata map is defensively
     * copied to ensure immutability.</p>
     *
     * @param successful whether all verification checks passed
     * @param verifiedAt when the verification was performed (must not be null)
     * @param passedChecks list of passed verification checks (must not be null)
     * @param failedChecks list of failed verification checks (must not be null)
     * @param metadata additional verification metadata (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public IdentityVerificationResult(
            boolean successful,
            Instant verifiedAt,
            List<String> passedChecks,
            List<String> failedChecks,
            Map<String, Object> metadata) {
        this.successful = successful;
        this.verifiedAt = Objects.requireNonNull(verifiedAt, "verifiedAt must not be null");
        this.passedChecks = new ArrayList<>(Objects.requireNonNull(passedChecks, "passedChecks must not be null"));
        this.failedChecks = new ArrayList<>(Objects.requireNonNull(failedChecks, "failedChecks must not be null"));
        this.metadata = new HashMap<>(Objects.requireNonNull(metadata, "metadata must not be null"));
    }

    /**
     * Returns whether all verification checks passed.
     *
     * @return {@code true} if all checks passed, {@code false} otherwise
     */
    public boolean successful() {
        return successful;
    }

    /**
     * Returns when the verification was performed.
     *
     * @return the verification timestamp (never null)
     */
    public Instant verifiedAt() {
        return verifiedAt;
    }

    /**
     * Returns an unmodifiable view of the passed checks.
     *
     * <p>The returned list is unmodifiable. Attempts to modify it will result
     * in an {@link UnsupportedOperationException}.</p>
     *
     * @return an unmodifiable list of passed checks (never null)
     */
    public List<String> passedChecks() {
        return Collections.unmodifiableList(passedChecks);
    }

    /**
     * Returns an unmodifiable view of the failed checks.
     *
     * <p>The returned list is unmodifiable. Attempts to modify it will result
     * in an {@link UnsupportedOperationException}.</p>
     *
     * @return an unmodifiable list of failed checks (never null)
     */
    public List<String> failedChecks() {
        return Collections.unmodifiableList(failedChecks);
    }

    /**
     * Returns an unmodifiable view of the verification metadata.
     *
     * <p>The returned map is unmodifiable. Attempts to modify it will result
     * in an {@link UnsupportedOperationException}.</p>
     *
     * @return an unmodifiable map of metadata (never null)
     */
    public Map<String, Object> metadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two IdentityVerificationResult objects are equal if they have the same
     * successful flag, verifiedAt timestamp, passedChecks, failedChecks, and metadata.</p>
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the obj argument; {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        IdentityVerificationResult that = (IdentityVerificationResult) obj;
        return successful == that.successful &&
               Objects.equals(verifiedAt, that.verifiedAt) &&
               Objects.equals(passedChecks, that.passedChecks) &&
               Objects.equals(failedChecks, that.failedChecks) &&
               Objects.equals(metadata, that.metadata);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(successful, verifiedAt, passedChecks, failedChecks, metadata);
    }

    /**
     * Returns a string representation of the object.
     *
     * <p>The string representation includes the successful flag, verifiedAt timestamp,
     * passed and failed checks counts, and metadata for debugging purposes.</p>
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "IdentityVerificationResult{" +
               "successful=" + successful +
               ", verifiedAt=" + verifiedAt +
               ", passedChecks=" + passedChecks.size() + " items" +
               ", failedChecks=" + failedChecks.size() + " items" +
               ", metadata=" + metadata +
               '}';
    }
}