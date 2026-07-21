package platform.kernels.multiagent.error;

/**
 * <b>MultiAgentErrorCode</b>
 *
 * <p>Canonical enumeration of Multi-Agent Kernel error codes.</p>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — Error Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-104, EIO-ARCH-001</p>
 *
 * <p>MultiAgentErrorCode defines the canonical error codes for the Multi-Agent Kernel.
 * Each code represents a category of errors that can occur in the system.</p>
 *
 * @since 1.0
 */
public enum MultiAgentErrorCode {
    /**
     * Error related to agent registration operations.
     */
    REGISTRATION_ERROR,

    /**
     * Error related to agent discovery operations.
     */
    DISCOVERY_ERROR,

    /**
     * Error related to capability management.
     */
    CAPABILITY_ERROR,

    /**
     * Error related to agent lifecycle management.
     */
    LIFECYCLE_ERROR,

    /**
     * Error related to agent communication.
     */
    COMMUNICATION_ERROR,

    /**
     * Error related to validation failures.
     */
    VALIDATION_ERROR,

    /**
     * General Multi-Agent Kernel error.
     */
    MULTI_AGENT_ERROR
}