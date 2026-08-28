package com.shreeai.os.platform.core.plugin.verification;

import java.util.Objects;

/**
 * <b>VerificationIssue</b>
 *
 * <p>Immutable issue reported during plugin verification within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Captures a single verification finding.</li>
 *   <li>Contains no business logic.</li>
 *   <li>Immutable by design.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * @see VerificationSeverity
 * @see VerificationResult
 */
public final class VerificationIssue {

    private final VerificationSeverity severity;
    private final String message;

    /**
     * Constructs a new {@code VerificationIssue}.
     *
     * @param severity the severity level (must not be null)
     * @param message  the human-readable message (must not be null or blank)
     * @throws NullPointerException     if severity is null
     * @throws IllegalArgumentException if message is null or blank
     */
    public VerificationIssue(VerificationSeverity severity, String message) {
        this.severity = Objects.requireNonNull(severity, "severity must not be null");
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be null or blank");
        }
        this.message = message;
    }

    /**
     * Returns the severity level of this issue.
     *
     * @return the severity
     */
    public VerificationSeverity severity() {
        return severity;
    }

    /**
     * Returns the human-readable message describing this issue.
     *
     * @return the message
     */
    public String message() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VerificationIssue that = (VerificationIssue) obj;
        return severity == that.severity && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        int result = severity.hashCode();
        result = 31 * result + message.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "VerificationIssue{" +
                "severity=" + severity +
                ", message='" + message + '\'' +
                '}';
    }
}