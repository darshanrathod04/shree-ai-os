package platform.kernels.multiagent.api;

import java.util.List;

import platform.kernels.multiagent.model.AgentCapability;
import platform.kernels.multiagent.model.AgentRequest;
import platform.kernels.multiagent.model.AgentResponse;

/**
 * <b>CapabilityRegistryService</b>
 *
 * <p>Service for capability registry management.
 * This interface defines contracts for registering and querying agent capabilities.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines capability registration contracts.</li>
 *   <li>Defines capability query contracts.</li>
 *   <li>Defines capability validation contracts.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only API contracts.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Multi-Agent Kernel — API Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> MAGENT-101, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public interface CapabilityRegistryService {

    /**
     * Registers a capability for an agent.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param capability the capability to register (must not be {@code null})
     * @return the registration response
     * @throws IllegalArgumentException if capability is {@code null}
     */
    AgentResponse registerCapability(AgentCapability capability);

    /**
     * Removes a capability from an agent.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param capability the capability to remove (must not be {@code null})
     * @return the removal response
     * @throws IllegalArgumentException if capability is {@code null}
     */
    AgentResponse removeCapability(AgentCapability capability);

    /**
     * Queries capabilities by criteria.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param criteria the query criteria (must not be {@code null})
     * @return list of matching capabilities
     * @throws IllegalArgumentException if criteria is {@code null}
     */
    List<AgentCapability> queryCapabilities(AgentRequest criteria);

    /**
     * Validates agent capabilities.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param agentId the agent identifier (must not be {@code null} or empty)
     * @return validation result
     * @throws IllegalArgumentException if agentId is {@code null} or empty
     */
    boolean validateCapabilities(String agentId);
}