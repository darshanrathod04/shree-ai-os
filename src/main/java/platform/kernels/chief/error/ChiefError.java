package platform.kernels.chief.error;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ChiefError</b>
 *
 * <p>Immutable value object representing one orchestration failure.
 * This class encapsulates error information for the Chief Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Represents one orchestration failure.</li>
 *   <li>Encapsulates error information.</li>
 *   <li>Provides immutable error data.</li>
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
 * <p><b>Ownership:</b> Chief Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-104, EIO-ARCH-001</p>
 *
 * @param code          the error code (must not be {@code null})
 * @param message       the error message (must not be {@code null})
 * @param component     the component where error occurred (must not be {@code null})
 * @param operation     the operation that failed (must not be {@code null})
 * @param metadata      additional error metadata (must not be {@code null})
 * @param timestamp     when the error occurred (must not be {@code null})
 *
 * @since 1.0
 */
public final class ChiefError {

    private final ChiefErrorCode code;
    private final String message;
    private final String component;
    private final String operation;
    private final Map<String, Object> metadata;
    private final Instant timestamp;

    /**
     * Constructs a {@code ChiefError} with the specified parameters.
     *
     * @param code          the error code (must not be {@code null})
     * @param message       the error message (must not be {@code null})
     * @param component     the component where error occurred (must not be {@code null})
     * @param operation     the operation that failed (must not be {@code null})
     * @param metadata      additional error metadata (must not be {@code null})
     * @param timestamp     when the error occurred (must not be {@code null})
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public ChiefError(
            ChiefErrorCode code,
            String message,
            String component,
            String operation,
            Map<String, Object> metadata,
            Instant timestamp) {
        if (code == null) {
            throw new IllegalArgumentException("ChiefError code must not be null");
        }
        if (message == null) {
            throw new IllegalArgumentException("ChiefError message must not be null");
        }
        if (component == null) {
            throw new IllegalArgumentException("ChiefError component must not be null");
        }
        if (operation == null) {
            throw new IllegalArgumentException("ChiefError operation must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("ChiefError metadata must not be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("ChiefError timestamp must not be null");
        }

        this.code = code;
        this.message = message;
        this.component = component;
        this.operation = operation;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
        this.timestamp = timestamp;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public ChiefErrorCode code() {
        return code;
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    public String message() {
        return message;
    }

    /**
     * Returns the component where the error occurred.
     *
     * @return the component
     */
    public String component() {
        return component;
    }

    /**
     * Returns the operation that failed.
     *
     * @return the operation
     */
    public String operation() {
        return operation;
    }

    /**
     * Returns an unmodifiable view of the error metadata.
     *
     * @return unmodifiable map of metadata
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Returns when the error occurred.
     *
     * @return the timestamp
     */
    public Instant timestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChiefError that = (ChiefError) obj;
        return Objects.equals(code, that.code) &&
                Objects.equals(message, that.message) &&
                Objects.equals(component, that.component) &&
                Objects.equals(operation, that.operation) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, component, operation, metadata, timestamp);
    }

    @Override
    public String toString() {
        return "ChiefError{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", component='" + component + '\'' +
                ", operation='" + operation + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}