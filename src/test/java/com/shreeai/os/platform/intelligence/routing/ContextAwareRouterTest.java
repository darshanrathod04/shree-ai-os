package com.shreeai.os.platform.intelligence.routing;

import com.shreeai.os.platform.intelligence.agent.Agent;
import com.shreeai.os.platform.intelligence.agent.AgentCapability;
import com.shreeai.os.platform.intelligence.agent.AgentRegistry;
import com.shreeai.os.platform.intelligence.agent.AgentStatus;
import com.shreeai.os.platform.runtime.observability.FeatureFlag;
import com.shreeai.os.platform.runtime.observability.FeatureFlags;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ContextAwareRouter Tests")
class ContextAwareRouterTest {

    private AgentRegistry registry;
    private FeatureFlags flags;
    private ContextAwareRouter router;

    @BeforeEach
    void setUp() {
        registry = new AgentRegistry();
        flags = new FeatureFlags();
        flags.set(FeatureFlag.CONTEXT_AWARE_ROUTING, true);
        router = new ContextAwareRouter(registry, flags);

        registry.register(Agent.of("planner", "Planner Agent",
                AgentCapability.PLANNING, AgentCapability.REASONING));
        registry.register(Agent.of("memory", "Memory Keeper",
                AgentCapability.MEMORY));
        registry.register(Agent.of("tool", "Tool Runner",
                AgentCapability.TOOL_EXECUTION));
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("shree.feature.CONTEXT_AWARE_ROUTING");
    }

    @Test
    @DisplayName("routes by explicit required capability")
    void routesByRequiredCapability() {
        Optional<RoutingTarget> target = router.route(
                RoutingContext.forCapability("remember this", AgentCapability.MEMORY));

        assertTrue(target.isPresent());
        assertEquals("memory", target.get().agent().id());
        assertEquals(AgentCapability.MEMORY, target.get().matchedCapability());
        assertEquals(0.95, target.get().confidence());
    }

    @Test
    @DisplayName("routes by heuristic on capability keyword in text")
    void routesByHeuristicOnCapabilityKeyword() {
        Optional<RoutingTarget> target = router.route(
                RoutingContext.of("please execute that tool now"));

        assertTrue(target.isPresent());
        assertEquals("tool", target.get().agent().id());
        assertEquals(AgentCapability.TOOL_EXECUTION, target.get().matchedCapability());
    }

    @Test
    @DisplayName("routes by heuristic on agent name in text")
    void routesByHeuristicOnAgentName() {
        Optional<RoutingTarget> target = router.route(
                RoutingContext.of("ask the memory keeper about it"));

        assertTrue(target.isPresent());
        assertEquals("memory", target.get().agent().id());
    }

    @Test
    @DisplayName("explicit capability routes to first matching agent")
    void explicitCapabilityFirstMatch() {
        registry.register(Agent.of("planner2", "Second Planner",
                AgentCapability.PLANNING));

        Optional<RoutingTarget> target = router.route(
                RoutingContext.forCapability("plan it", AgentCapability.PLANNING));

        assertTrue(target.isPresent());
        assertEquals("planner", target.get().agent().id());
    }

    @Test
    @DisplayName("returns empty when no agent declares required capability")
    void emptyWhenNoCapabilityAgent() {
        Optional<RoutingTarget> target = router.route(
                RoutingContext.forCapability("coordinate", AgentCapability.COORDINATION));

        assertEquals(Optional.empty(), target);
    }

    @Test
    @DisplayName("returns empty when feature flag is disabled")
    void emptyWhenFlagDisabled() {
        flags.set(FeatureFlag.CONTEXT_AWARE_ROUTING, false);

        Optional<RoutingTarget> target = router.route(
                RoutingContext.forCapability("remember", AgentCapability.MEMORY));

        assertEquals(Optional.empty(), target);
    }

    @Test
    @DisplayName("suspended agents are not routed to")
    void suspendedAgentsNotRouted() {
        registry.updateStatus("planner", AgentStatus.SUSPENDED);

        Optional<RoutingTarget> target = router.route(
                RoutingContext.forCapability("plan", AgentCapability.PLANNING));

        // Only tool/memory are ACTIVE; planner is suspended
        assertEquals(Optional.empty(), target);
    }

    @Test
    @DisplayName("returns empty when no dispatchable agents")
    void emptyWhenNoDispatchableAgents() {
        AgentRegistry empty = new AgentRegistry();
        ContextAwareRouter noAgentRouter = new ContextAwareRouter(empty, flags);

        Optional<RoutingTarget> target = noAgentRouter.route(
                RoutingContext.of("hello"));

        assertEquals(Optional.empty(), target);
    }

    @Test
    @DisplayName("returns empty for unrelated text with low confidence")
    void emptyForUnrelatedText() {
        Optional<RoutingTarget> target = router.route(
                RoutingContext.of("what is the weather today"));

        // No capability/name keyword matches
        assertEquals(Optional.empty(), target);
    }

    @Test
    @DisplayName("null context throws")
    void nullContextThrows() {
        assertThrows(NullPointerException.class, () -> router.route(null));
    }

    @Test
    @DisplayName("target confidence is clamped into [0,1]")
    void targetConfidenceClamped() {
        RoutingTarget target = RoutingTarget.builder()
                .agent(registry.findById("tool").get())
                .confidence(5.0)
                .build();
        assertEquals(1.0, target.confidence());
    }

    @Test
    @DisplayName("routing target conveys agent and reason")
    void routingTargetConveysAgentAndReason() {
        Optional<RoutingTarget> target = router.route(
                RoutingContext.forCapability("remember", AgentCapability.MEMORY));

        assertTrue(target.get().reason().contains("explicit capability match"));
        assertTrue(target.get().toString().contains("memory"));
    }
}
