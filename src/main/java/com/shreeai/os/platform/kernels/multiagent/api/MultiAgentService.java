package com.shreeai.os.platform.kernels.multiagent.api;

import java.util.List;

import com.shreeai.os.platform.kernels.multiagent.model.AgentCommunication;
import com.shreeai.os.platform.kernels.multiagent.model.AgentDescriptor;
import com.shreeai.os.platform.kernels.multiagent.model.AgentRequest;
import com.shreeai.os.platform.kernels.multiagent.model.AgentResponse;
import com.shreeai.os.platform.kernels.multiagent.model.MultiAgentMetrics;

/**
 * <b>MultiAgentService</b>
 *
 * <p>Primary façade for the Multi-Agent Kernel.
 * This interface defines the main entry points for agent orchestration.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides primary entry point for agent operations.</li>
 *   <li>Coordinates specialized agent services.</li>
 *   <li>Delegates to Chief Kernel for all coordination.</li>
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
public interface MultiAgentService {

    /**
     * Registers a new agent with the kernel.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param request the agent registration request (must not be {@code null})
     * @return the agent registration response
     * @throws IllegalArgumentException if request is {@code null}
     */
    AgentResponse registerAgent(AgentRequest request);

    /**
     * Unregisters an agent from the kernel.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param agentId the agent identifier (must not be {@code null} or empty)
     * @return the unregistration response
     * @throws IllegalArgumentException if agentId is {@code null} or empty
     */
    AgentResponse unregisterAgent(String agentId);

    /**
     * Discovers agents based on criteria.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param criteria the discovery criteria (must not be {@code null})
     * @return list of matching agent descriptors
     * @throws IllegalArgumentException if criteria is {@code null}
     */
    List<AgentDescriptor> discoverAgents(AgentRequest criteria);

    /**
     * Sends a communication message through the Chief Kernel.
     *
     * <p>All communication MUST flow through the Chief Kernel.
     * Direct agent-to-agent communication is forbidden.</p>
     *
     * @param communication the communication message (must not be {@code null})
     * @return the communication response
     * @throws IllegalArgumentException if communication is {@code null}
     */
    AgentResponse communicate(AgentCommunication communication);

    /**
     * Retrieves the health status of the Multi-Agent Kernel.
     *
     * @return kernel health metrics
     */
    MultiAgentMetrics getKernelHealth();
}