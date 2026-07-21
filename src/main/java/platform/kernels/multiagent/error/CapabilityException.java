package platform.kernels.multiagent.error;

/**
 * <b>CapabilityException</b>
 *
 * <p>Exception thrown when capability management errors occur.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-104, EIO-ARCH-001</p>
 *
 * <p>CapabilityException represents capability-specific errors.
 * It wraps a MultiAgentError with CAPABILITY_ERROR code.</p>
 *
 * @param error the error object (must not be {@code null})
 *
 * @since 1.0
 */
public class CapabilityException extends MultiAgentException {
    /**
     * Creates a new CapabilityException with the specified error.
     *
     * @param error the error object (must not be {@code null})
     * @throws NullPointerException if error is {@code null}
     * @since 1.0
     */
    public CapabilityException(MultiAgentError error) {
        super(error);
    }

    /**
     * Creates a new CapabilityException with the specified error and cause.
     *
     * @param error the error object (must not be {@code null})
     * @param cause the cause of the exception (may be {@code null})
     * @throws NullPointerException if error is {@code null}
     * @since 1.0
     */
    public CapabilityException(MultiAgentError error, Throwable cause) {
        super(error, cause);
    }
}