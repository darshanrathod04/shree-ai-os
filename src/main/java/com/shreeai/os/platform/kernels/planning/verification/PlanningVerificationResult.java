package com.shreeai.os.platform.kernels.planning.verification;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>PlanningVerificationResult</b>
 *
 * <p>Immutable value object representing the result of a planning architecture verification.
 * This result captures whether the verification passed, any findings discovered, the
 * timestamp of verification, and associated metadata.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a structured representation of verification outcomes.</li>
 *   <li>Supports multiple findings in a single verification execution.</li>
 *   <li>Enables callers to inspect architectural violations without exception handling.</li>
 *   <li>Records the timestamp when verification was performed.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final with no setters.</li>
 *   <li>Constructor validation — rejects {@code null} arguments.</li>
 *   <li>Defensive copying — collections are copied on construction.</li>
 *   <li>Value-based equality — implements {@link #equals(Object)} and {@link #hashCode()}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel — Verification Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-107, EIO-ARCH-001</p>
 *
 * @param successful  whether the verification passed
 * @param findings    the list of finding messages (must not be {@code null})
 * @param verifiedAt  the verification timestamp (must not be {@code null})
 * @param metadata    additional metadata (must not be {@code null})
 */
public final class PlanningVerificationResult {

    private final boolean successful;
    private final List<String> findings;
    private final Instant verifiedAt;
    private final Map<String, Object> metadata;

    /**
     * Constructs a {@code PlanningVerificationResult} with the specified parameters.
     *
     * @param successful whether the verification passed
     * @param findings   the list of finding messages (must not be {@code null})
     * @param verifiedAt the verification timestamp (must not be {@code null})
     * @param metadata   additional metadata (must not be {@code null})
     * @throws NullPointerException if {@code findings}, {@code verifiedAt}, or {@code metadata} is {@code null}
     */
    public PlanningVerificationResult(boolean successful,
                                      List<String> findings,
                                      Instant verifiedAt,
                                      Map<String, Object> metadata) {
        this.successful = successful;
        this.findings = List.copyOf(
                Objects.requireNonNull(findings, "findings must not be null"));
        this.verifiedAt = Objects.requireNonNull(verifiedAt, "verifiedAt must not be null");
        this.metadata = Collections.unmodifiableMap(
                Objects.requireNonNull(metadata, "metadata must not be null"));
    }

    /**
     * Returns whether this verification result is successful.
     *
     * @return {@code true} if verification passed with no findings, {@code false} otherwise
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Returns an unmodifiable view of the finding messages.
     *
     * @return the list of findings (empty if verification passed)
     */
    public List<String> findings() {
        return findings;
    }

    /**
     * Returns the timestamp when verification was performed.
     *
     * @return the verification {@link Instant}
     */
    public Instant verifiedAt() {
        return verifiedAt;
    }

    /**
     * Returns an unmodifiable view of the metadata.
     *
     * @return the metadata map
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanningVerificationResult that)) return false;
        return successful == that.successful
                && findings.equals(that.findings)
                && verifiedAt.equals(that.verifiedAt)
                && metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        int result = (successful ? 1 : 0);
        result = 31 * result + findings.hashCode();
        result = 31 * result + verifiedAt.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PlanningVerificationResult{"
                + "successful=" + successful
                + ", findings=" + findings
                + ", verifiedAt=" + verifiedAt
                + ", metadata=" + metadata
                + '}';
    }
}