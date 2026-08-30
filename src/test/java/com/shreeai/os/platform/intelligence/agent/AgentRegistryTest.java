package com.shreeai.os.platform.intelligence.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AgentRegistry Tests")
class AgentRegistryTest {

    private AgentRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new AgentRegistry();
    }

    private Agent plannerAgent() {
        return Agent.of("agent-1", "Planner",
                AgentCapability.PLANNING, AgentCapability.REASONING);
    }

    @Test
    @DisplayName("register adds an agent")
    void registerAddsAgent() {
        Optional<Agent> previous = registry.register(plannerAgent());
        assertEquals(Optional.empty(), previous);
        assertEquals(1, registry.size());
        assertTrue(registry.isRegistered("agent-1"));
    }

    @Test
    @DisplayName("register replaces existing agent with same id")
    void registerReplacesSameId() {
        registry.register(plannerAgent());
        Agent replacement = Agent.of("agent-1", "NewPlanner", AgentCapability.PLANNING);
        Optional<Agent> previous = registry.register(replacement);

        assertEquals("Planner", previous.get().name());
        assertEquals(1, registry.size());
        assertEquals("NewPlanner", registry.findById("agent-1").get().name());
    }

    @Test
    @DisplayName("findById returns matching agent")
    void findByIdMatches() {
        registry.register(plannerAgent());
        Optional<Agent> found = registry.findById("agent-1");
        assertTrue(found.isPresent());
        assertEquals("agent-1", found.get().id());
    }

    @Test
    @DisplayName("findById returns empty for unknown id")
    void findByIdUnknown() {
        assertEquals(Optional.empty(), registry.findById("missing"));
    }

    @Test
    @DisplayName("findByName is case-insensitive")
    void findByNameCaseInsensitive() {
        registry.register(plannerAgent());
        assertTrue(registry.findByName("planner").isPresent());
        assertTrue(registry.findByName("PLANNER").isPresent());
    }

    @Test
    @DisplayName("findByName returns empty for unknown name")
    void findByNameUnknown() {
        assertEquals(Optional.empty(), registry.findByName("nobody"));
    }

    @Test
    @DisplayName("findByCapability returns agents declaring it")
    void findByCapabilityMatches() {
        registry.register(plannerAgent());
        registry.register(Agent.of("agent-2", "MemoryKeeper", AgentCapability.MEMORY));

        List<Agent> planners = registry.findByCapability(AgentCapability.REASONING);
        assertEquals(1, planners.size());
        assertEquals("agent-1", planners.get(0).id());

        List<Agent> memory = registry.findByCapability(AgentCapability.MEMORY);
        assertEquals(1, memory.size());
        assertEquals("agent-2", memory.get(0).id());
    }

    @Test
    @DisplayName("unregister removes an agent")
    void unregisterRemoves() {
        registry.register(plannerAgent());
        Optional<Agent> removed = registry.unregister("agent-1");
        assertTrue(removed.isPresent());
        assertEquals(0, registry.size());
        assertFalse(registry.isRegistered("agent-1"));
    }

    @Test
    @DisplayName("updateStatus changes agent lifecycle state")
    void updateStatusChanges() {
        registry.register(plannerAgent());
        assertTrue(registry.updateStatus("agent-1", AgentStatus.SUSPENDED));

        assertEquals(AgentStatus.SUSPENDED, registry.findById("agent-1").get().status());
        assertFalse(registry.findById("agent-1").get().isDispatchable());
    }

    @Test
    @DisplayName("updateStatus on unknown agent returns false")
    void updateStatusUnknown() {
        assertFalse(registry.updateStatus("missing", AgentStatus.ACTIVE));
    }

    @Test
    @DisplayName("dispatchable returns only ACTIVE agents")
    void dispatchableFiltersByStatus() {
        registry.register(plannerAgent());
        registry.register(Agent.of("agent-2", "Keeper", AgentCapability.MEMORY));
        registry.updateStatus("agent-2", AgentStatus.RETIRED);

        List<Agent> dispatchable = registry.dispatchable();
        assertEquals(1, dispatchable.size());
        assertEquals("agent-1", dispatchable.get(0).id());
    }

    @Test
    @DisplayName("all returns immutable snapshot")
    void allReturnsSnapshot() {
        registry.register(plannerAgent());
        List<Agent> snapshot = registry.all();
        assertEquals(1, snapshot.size());
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add(Agent.of("x", "y", AgentCapability.PLANNING)));
    }

    @Test
    @DisplayName("null id register throws")
    void nullRegisterThrows() {
        assertThrows(NullPointerException.class, () -> registry.register(null));
        assertThrows(NullPointerException.class, () -> registry.findById(null));
        assertThrows(NullPointerException.class, () -> registry.findByName(null));
        assertThrows(NullPointerException.class, () -> registry.findByCapability(null));
        assertThrows(NullPointerException.class, () -> registry.unregister(null));
    }

    @Test
    @DisplayName("agent with duplicate capabilities deduplicates")
    void agentCapabilitiesDeduplicate() {
        Agent agent = Agent.of("a", "A", AgentCapability.PLANNING, AgentCapability.PLANNING);
        assertEquals(1, agent.capabilities().size());
    }

    @Test
    @DisplayName("agent equality is by id")
    void agentEqualityById() {
        Agent a = Agent.of("id", "One", AgentCapability.PLANNING);
        Agent b = Agent.of("id", "Two", AgentCapability.MEMORY);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("agent without id throws")
    void agentWithoutIdThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> Agent.builder().name("no-id").build());
    }
}
