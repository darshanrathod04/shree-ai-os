package platform.kernels.multiagent.error;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * <b>MultiAgentException</b>
 *
 * <p>Canonical base runtime exception for the Multi-Agent Kernel.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-104, EIO-ARCH-001</p>
 *
 * <p>MultiAgentException wraps a MultiAgentError and exposes immutable error information.
 * It is the base class for all specialized Multi-Agent exceptions.</p>
 *
 * @param error    the error object (must not be {@code null})
 * @param cause    the cause of the exception (may be {@code null})
 *
 * @since 1.0
 */
public class MultiAgentException extends RuntimeException {
    private final MultiAgentError error;

    /**
     * Creates a new MultiAgentException with the specified error.
     *
     * @param error the error object (must not be {@code null})
     * @throws NullPointerException if error is {@code null}
     * @since 1.0
     */
    public MultiAgentException(MultiAgentError error) {
        super(Objects.requireNonNull(error, "MultiAgentException error must not be null").message());
        this.error = error;
    }

    /**
     * Creates a new MultiAgentException with the specified error and cause.
     *
     * @param error the error object (must not be {@code null})
     * @param cause the cause of the exception (may be {@code null})
     * @throws NullPointerException if error is {@code null}
     * @since 1.0
     */
    public MultiAgentException(MultiAgentError error, Throwable cause) {
        super(Objects.requireNonNull(error, "MultiAgentException error must not be null").message(), cause);
        this.error = error;
    }

    /**
     * Returns the error object.
     *
     * @return the error object
     * @since 1.0
     */
    public MultiAgentError error() {
        return error;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     * @since 1.0
     */
    public MultiAgentErrorCode errorCode() {
        return error.errorCode();
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     * @since 1.0
     */
    @Override
    public String getMessage() {
        return error.message();
    }

    /**
     * Returns the agent identifier.
     *
     * @return the agent identifier, or {@code null} if not present
     * @since 1.0
     */
    public String agentId() {
        return error.agentId();
    }

    /**
     * Returns when the error occurred.
     *
     * @return the error timestamp
     * @since 1.0
     */
    public Instant timestamp() {
        return error.timestamp();
    }

    /**
     * Returns the error details.
     *
     * @return an unmodifiable view of the error details
     * @since 1.0
     */
    public Map<String, Object> details() {
        return error.details();
    }
}