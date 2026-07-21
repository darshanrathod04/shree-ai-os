package platform.kernels.multiagent.api;

import java.util.List;

import platform.kernels.multiagent.model.AgentCapability;
import platform.kernels.multiagent.model.AgentDescriptor;
import platform.kernels.multiagent.model.AgentStatus;

/**
 * <b>AgentDiscoveryService</b>
 *
 * <p>Service for agent discovery.
 * This interface defines contracts for discovering agents by various criteria.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines agent discovery contracts.</li>
 *   <li>Defines capability-based discovery contracts.</li>
 *   <li>Defines metadata-based discovery contracts.</li>
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
public interface AgentDiscoveryService {

    /**
     * Discovers agents by capability.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param capability the capability to search for (must not be {@code null})
     * @return list of agents with the specified capability
     * @throws IllegalArgumentException if capability is {@code null}
     */
    List<AgentDescriptor> discoverByCapability(AgentCapability capability);

    /**
     * Discovers agents by status.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param status the status to filter by (must not be {@code null})
     * @return list of agents with the specified status
     * @throws IllegalArgumentException if status is {@code null}
     */
    List<AgentDescriptor> discoverByStatus(AgentStatus status);

    /**
     * Discovers agents by metadata.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param metadata the metadata criteria (must not be {@code null})
     * @return list of agents matching the metadata criteria
     * @throws IllegalArgumentException if metadata is {@code null}
     */
    List<AgentDescriptor> discoverByMetadata(java.util.Map<String, Object> metadata);

    /**
     * Lists all available agents.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @return list of all available agents
     */
    List<AgentDescriptor> listAvailableAgents();
}