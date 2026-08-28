package com.shreeai.os.platform.kernels.multiagent.api;

import com.shreeai.os.platform.kernels.multiagent.model.AgentCommunication;
import com.shreeai.os.platform.kernels.multiagent.model.AgentResponse;
import com.shreeai.os.platform.kernels.multiagent.model.AgentStatus;

/**
 * <b>AgentCommunicationService</b>
 *
 * <p>Service for agent communication.
 * This interface defines contracts for agent communication through the Chief Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines agent communication contracts.</li>
 *   <li>Enforces Chief Kernel routing for all communication.</li>
 *   <li>Defines communication status contracts.</li>
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
public interface AgentCommunicationService {

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
    AgentResponse send(AgentCommunication communication);

    /**
     * Receives a communication message through the Chief Kernel.
     *
     * <p>All communication MUST flow through the Chief Kernel.
     * Direct agent-to-agent communication is forbidden.</p>
     *
     * @param agentId the receiving agent identifier (must not be {@code null} or empty)
     * @return the received communication
     * @throws IllegalArgumentException if agentId is {@code null} or empty
     */
    AgentCommunication receive(String agentId);

    /**
     * Routes a communication through the Chief Kernel.
     *
     * <p>All communication MUST flow through the Chief Kernel.
     * Direct agent-to-agent communication is forbidden.</p>
     *
     * @param communication the communication message (must not be {@code null})
     * @return the routing response
     * @throws IllegalArgumentException if communication is {@code null}
     */
    AgentResponse routeThroughChief(AgentCommunication communication);

    /**
     * Retrieves the communication status for an agent.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param agentId the agent identifier (must not be {@code null} or empty)
     * @return the communication status
     * @throws IllegalArgumentException if agentId is {@code null} or empty
     */
    AgentStatus getCommunicationStatus(String agentId);
}