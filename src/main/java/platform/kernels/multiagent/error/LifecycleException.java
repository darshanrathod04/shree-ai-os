package platform.kernels.multiagent.error;

/**
 * <b>LifecycleException</b>
 *
 * <p>Exception thrown when agent lifecycle errors occur.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-104, EIO-ARCH-001</p>
 *
 * <p>LifecycleException represents lifecycle-specific errors.
 * It wraps a MultiAgentError with LIFECYCLE_ERROR code.</p>
 *
 * @param error the error object (must not be {@code null})
 *
 * @since 1.0
 */
public class LifecycleException extends MultiAgentException {
    /**
     * Creates a new LifecycleException with the specified error.
     *
     * @param error the error object (must not be {@code null})
     * @throws NullPointerException if error is {@code null}
     * @since 1.0
     */
    public LifecycleException(MultiAgentError error) {
        super(error);
    }

    /**
     * Creates a new LifecycleException with the specified error and cause.
     *
     * @param error the error object (must not be {@code null})
     * @param cause the cause of the exception (may be {@code null})
     * @throws NullPointerException if error is {@code null}
     * @since 1.0
     */
    public LifecycleException(MultiAgentError error, Throwable cause) {
        super(error, cause);
    }
}