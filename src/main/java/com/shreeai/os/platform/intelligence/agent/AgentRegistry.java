package com.shreeai.os.platform.intelligence.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * <b>AgentRegistry</b>
 *
 * <p>Thread-safe registry of {@link Agent}s for the platform. Agents can be
 * registered, unregistered, looked up by id/name/capability, and queried for
 * dispatch eligibility. This underpins the V3 context-aware routing and
 * orchestration layers.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Maintains a canonical, stable id-to-agent mapping.</li>
 *   <li>Provides capability-based and name-based lookups.</li>
 *   <li>Exposes dispatchable (ACTIVE) agents for the router.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Intelligence — Agent Registry</p>
 * <p><b>Version:</b> 3.0</p>
 *
 * @since 3.0
 */
public final class AgentRegistry {

    private final Map<String, Agent> agentsById = new LinkedHashMap<>();

    /**
     * Registers (or replaces) an agent keyed by its id.
     *
     * @param agent the agent (never null)
     * @return the previously registered agent with the same id, or empty
     */
    public synchronized Optional<Agent> register(Agent agent) {
        Objects.requireNonNull(agent, "agent must not be null");
        Agent previous = agentsById.put(agent.id(), agent);
        return Optional.ofNullable(previous);
    }

    /**
     * Unregisters an agent by id.
     *
     * @param agentId the agent id (never null)
     * @return the removed agent, or empty if not present
     */
    public synchronized Optional<Agent> unregister(String agentId) {
        Objects.requireNonNull(agentId, "agentId must not be null");
        return Optional.ofNullable(agentsById.remove(agentId));
    }

    /**
     * Looks up an agent by its stable id.
     *
     * @param agentId the agent id (never null)
     * @return the matching agent, or empty
     */
    public synchronized Optional<Agent> findById(String agentId) {
        Objects.requireNonNull(agentId, "agentId must not be null");
        return Optional.ofNullable(agentsById.get(agentId));
    }

    /**
     * Looks up an agent by its name (case-insensitive).
     *
     * @param name the agent name (never null)
     * @return the matching agent, or empty
     */
    public synchronized Optional<Agent> findByName(String name) {
        Objects.requireNonNull(name, "name must not be null");
        String target = name.trim().toLowerCase();
        return agentsById.values().stream()
                .filter(a -> a.name().toLowerCase().equals(target))
                .findFirst();
    }

    /**
     * Returns all agents that declare the given capability.
     *
     * @param capability the capability (never null)
     * @return matching agents (never null)
     */
    public synchronized List<Agent> findByCapability(AgentCapability capability) {
        Objects.requireNonNull(capability, "capability must not be null");
        return agentsById.values().stream()
                .filter(a -> a.hasCapability(capability))
                .toList();
    }

    /**
     * Returns all agents that are eligible for dispatch (ACTIVE).
     *
     * @return dispatchable agents (never null)
     */
    public synchronized List<Agent> dispatchable() {
        return agentsById.values().stream()
                .filter(Agent::isDispatchable)
                .toList();
    }

    /**
     * Updates the lifecycle status of a registered agent.
     *
     * @param agentId the agent id (never null)
     * @param status  the new status (never null)
     * @return true if an agent was updated
     */
    public synchronized boolean updateStatus(String agentId, AgentStatus status) {
        Objects.requireNonNull(agentId, "agentId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Agent existing = agentsById.get(agentId);
        if (existing == null) {
            return false;
        }
        agentsById.put(agentId, Agent.builder()
                .id(existing.id())
                .name(existing.name())
                .description(existing.description())
                .capabilities(existing.capabilities())
                .status(status)
                .registeredAt(existing.registeredAt())
                .build());
        return true;
    }

    /**
     * @return whether an agent with the given id is registered
     */
    public synchronized boolean isRegistered(String agentId) {
        Objects.requireNonNull(agentId, "agentId must not be null");
        return agentsById.containsKey(agentId);
    }

    /**
     * @return the number of registered agents
     */
    public synchronized int size() {
        return agentsById.size();
    }

    /**
     * Returns an immutable snapshot of all registered agents.
     *
     * @return all agents (never null)
     */
    public synchronized List<Agent> all() {
        return List.copyOf(agentsById.values());
    }
}
