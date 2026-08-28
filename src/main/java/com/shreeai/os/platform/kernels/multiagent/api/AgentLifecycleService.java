package com.shreeai.os.platform.kernels.multiagent.api;

import com.shreeai.os.platform.kernels.multiagent.model.AgentResponse;
import com.shreeai.os.platform.kernels.multiagent.model.AgentStatus;

/**
 * <b>AgentLifecycleService</b>
 *
 * <p>Service for agent lifecycle management.
 * This interface defines contracts for managing agent lifecycles.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines agent lifecycle contracts.</li>
 *   <li>Defines agent state transition contracts.</li>
 *   <li>Defines lifecycle query contracts.</li>
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
public interface AgentLifecycleService {

    /**
     * Starts an agent.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param agentId the agent identifier (must not be {@code null} or empty)
     * @return the lifecycle response
     * @throws IllegalArgumentException if agentId is {@code null} or empty
     */
    AgentResponse start(String agentId);

    /**
     * Stops an agent.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param agentId the agent identifier (must not be {@code null} or empty)
     * @return the lifecycle response
     * @throws IllegalArgumentException if agentId is {@code null} or empty
     */
    AgentResponse stop(String agentId);

    /**
     * Pauses an agent.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param agentId the agent identifier (must not be {@code null} or empty)
     * @return the lifecycle response
     * @throws IllegalArgumentException if agentId is {@code null} or empty
     */
    AgentResponse pause(String agentId);

    /**
     * Resumes a paused agent.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param agentId the agent identifier (must not be {@code null} or empty)
     * @return the lifecycle response
     * @throws IllegalArgumentException if agentId is {@code null} or empty
     */
    AgentResponse resume(String agentId);

    /**
     * Retrieves the lifecycle state of an agent.
     *
     * <p>All coordination flows through the Chief Kernel.</p>
     *
     * @param agentId the agent identifier (must not be {@code null} or empty)
     * @return the agent lifecycle state
     * @throws IllegalArgumentException if agentId is {@code null} or empty
     */
    AgentStatus getLifecycle(String agentId);
}