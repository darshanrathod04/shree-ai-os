package platform.kernels.multiagent.error;

/**
 * <b>CommunicationException</b>
 *
 * <p>Exception thrown when agent communication errors occur.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-104, EIO-ARCH-001</p>
 *
 * <p>CommunicationException represents communication-specific errors.
 * It wraps a MultiAgentError with COMMUNICATION_ERROR code.</p>
 *
 * @param error the error object (must not be {@code null})
 *
 * @since 1.0
 */
public class CommunicationException extends MultiAgentException {
    /**
     * Creates a new CommunicationException with the specified error.
     *
     * @param error the error object (must not be {@code null})
     * @throws NullPointerException if error is {@code null}
     * @since 1.0
     */
    public CommunicationException(MultiAgentError error) {
        super(error);
    }

    /**
     * Creates a new CommunicationException with the specified error and cause.
     *
     * @param error the error object (must not be {@code null})
     * @param cause the cause of the exception (may be {@code null})
     * @throws NullPointerException if error is {@code null}
     * @since 1.0
     */
    public CommunicationException(MultiAgentError error, Throwable cause) {
        super(error, cause);
    }
}