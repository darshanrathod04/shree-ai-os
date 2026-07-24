package platform.kernels.multiagent.api;

import java.util.List;

import platform.kernels.multiagent.model.AgentRegistration;
import platform.kernels.multiagent.model.AgentResponse;

/**
 * <b>AgentRegistryService</b>
 *
 * <p>Service for agent registration management.
 * This interface defines contracts for registering and managing agent registrations.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines agent registration contracts.</li>
 *   <li>Defines agent unregistration contracts.</li>
 *   <li>Defines registration query contracts.</li>
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
public interface AgentRegistryService {

    /**
     * Registers a new agent.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param registration the agent registration (must not be {@code null})
     * @return the registration response
     * @throws IllegalArgumentException if registration is {@code null}
     */
    AgentResponse register(AgentRegistration registration);

    /**
     * Unregisters an agent.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param agentId the agent identifier (must not be {@code null} or empty)
     * @return the unregistration response
     * @throws IllegalArgumentException if agentId is {@code null} or empty
     */
    AgentResponse unregister(String agentId);

    /**
     * Updates an existing agent registration.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param registration the updated registration (must not be {@code null})
     * @return the update response
     * @throws IllegalArgumentException if registration is {@code null}
     */
    AgentResponse updateRegistration(AgentRegistration registration);

    /**
     * Finds a specific agent registration.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param agentId the agent identifier (must not be {@code null} or empty)
     * @return the agent registration, or {@code null} if not found
     * @throws IllegalArgumentException if agentId is {@code null} or empty
     */
    AgentRegistration findRegistration(String agentId);

    /**
     * Lists all agent registrations.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @return list of all agent registrations
     */
    List<AgentRegistration> listRegistrations();
}