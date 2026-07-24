package platform.kernels.multiagent.error;

/**
 * <b>AgentDiscoveryException</b>
 *
 * <p>Exception thrown when agent discovery errors occur.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-104, EIO-ARCH-001</p>
 *
 * <p>AgentDiscoveryException represents discovery-specific errors.
 * It wraps a MultiAgentError with DISCOVERY_ERROR code.</p>
 *
 * @param error the error object (must not be {@code null})
 *
 * @since 1.0
 */
public class AgentDiscoveryException extends MultiAgentException {
    /**
     * Creates a new AgentDiscoveryException with the specified error.
     *
     * @param error the error object (must not be {@code null})
     * @throws NullPointerException if error is {@code null}
     * @since 1.0
     */
    public AgentDiscoveryException(MultiAgentError error) {
        super(error);
    }

    /**
     * Creates a new AgentDiscoveryException with the specified error and cause.
     *
     * @param error the error object (must not be {@code null})
     * @param cause the cause of the exception (may be {@code null})
     * @throws NullPointerException if error is {@code null}
     * @since 1.0
     */
    public AgentDiscoveryException(MultiAgentError error, Throwable cause) {
        super(error, cause);
    }
}