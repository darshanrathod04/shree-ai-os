package com.shreeai.os.platform.kernels.multiagent.error;

/**
 * <b>AgentRegistrationException</b>
 *
 * <p>Exception thrown when agent registration errors occur.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-104, EIO-ARCH-001</p>
 *
 * <p>AgentRegistrationException represents registration-specific errors.
 * It wraps a MultiAgentError with REGISTRATION_ERROR code.</p>
 *
 * @param error the error object (must not be {@code null})
 *
 * @since 1.0
 */
public class AgentRegistrationException extends MultiAgentException {
    /**
     * Creates a new AgentRegistrationException with the specified error.
     *
     * @param error the error object (must not be {@code null})
     * @throws NullPointerException if error is {@code null}
     * @since 1.0
     */
    public AgentRegistrationException(MultiAgentError error) {
        super(error);
    }

    /**
     * Creates a new AgentRegistrationException with the specified error and cause.
     *
     * @param error the error object (must not be {@code null})
     * @param cause the cause of the exception (may be {@code null})
     * @throws NullPointerException if error is {@code null}
     * @since 1.0
     */
    public AgentRegistrationException(MultiAgentError error, Throwable cause) {
        super(error, cause);
    }
}