package com.shreeai.os.platform.core.configuration.engine;

import com.shreeai.os.platform.core.configuration.model.ConfigurationEntry;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>ResolutionResult</b>
 *
 * <p>Immutable result of configuration resolution within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates the result of configuration resolution.</li>
 *   <li>Provides success/failure status with optional resolved value.</li>
 *   <li>Enables consistent resolution reporting across the Platform.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All fields are non-null.</p>
 */
public final class ResolutionResult {

    private final boolean success;
    private final ConfigurationEntry resolvedEntry;
    private final Object resolvedValue;
    private final String failureMessage;
    private final Instant timestamp;

    /**
     * Constructs a successful {@code ResolutionResult}.
     *
     * @param resolvedEntry the resolved configuration entry (must not be null)
     * @param resolvedValue the resolved value (must not be null)
     * @param timestamp the resolution timestamp (must not be null)
     * @return a successful ResolutionResult
     */
    public static ResolutionResult success(ConfigurationEntry resolvedEntry, Object resolvedValue, Instant timestamp) {
        return new ResolutionResult(true, resolvedEntry, resolvedValue, null, timestamp);
    }

    /**
     * Constructs a failed {@code ResolutionResult}.
     *
     * @param failureMessage the failure message (must not be null or blank)
     * @param timestamp the resolution timestamp (must not be null)
     * @return a failed ResolutionResult
     */
    public static ResolutionResult failure(String failureMessage, Instant timestamp) {
        return new ResolutionResult(false, null, null, failureMessage, timestamp);
    }

    /**
     * Private constructor for ResolutionResult.
     *
     * @param success whether resolution succeeded
     * @param resolvedEntry the resolved configuration entry (null if failed)
     * @param resolvedValue the resolved value (null if failed)
     * @param failureMessage the failure message (null if successful)
     * @param timestamp the resolution timestamp
     */
    private ResolutionResult(boolean success,
                             ConfigurationEntry resolvedEntry,
                             Object resolvedValue,
                             String failureMessage,
                             Instant timestamp) {
        this.success = success;
        this.resolvedEntry = resolvedEntry;
        this.resolvedValue = resolvedValue;
        this.failureMessage = failureMessage;
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp must not be null");
    }

    /**
     * Returns whether the resolution succeeded.
     *
     * @return {@code true} if resolution succeeded, {@code false} otherwise
     */
    public boolean success() {
        return success;
    }

    /**
     * Returns the resolved configuration entry.
     *
     * @return the resolved configuration entry, or {@code null} if resolution failed
     */
    public ConfigurationEntry resolvedEntry() {
        return resolvedEntry;
    }

    /**
     * Returns the resolved value.
     *
     * @return the resolved value, or {@code null} if resolution failed
     */
    public Object resolvedValue() {
        return resolvedValue;
    }

    /**
     * Returns the failure message.
     *
     * @return the failure message, or {@code null} if resolution succeeded
     */
    public String failureMessage() {
        return failureMessage;
    }

    /**
     * Returns the resolution timestamp.
     *
     * @return the resolution timestamp
     */
    public Instant timestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResolutionResult that = (ResolutionResult) o;
        return success == that.success
                && Objects.equals(resolvedEntry, that.resolvedEntry)
                && Objects.equals(resolvedValue, that.resolvedValue)
                && Objects.equals(failureMessage, that.failureMessage)
                && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, resolvedEntry, resolvedValue, failureMessage, timestamp);
    }

    @Override
    public String toString() {
        return "ResolutionResult{"
                + "success=" + success
                + ", resolvedEntry=" + resolvedEntry
                + ", resolvedValue=" + resolvedValue
                + ", failureMessage='" + failureMessage + '\''
                + ", timestamp=" + timestamp
                + '}';
    }
}