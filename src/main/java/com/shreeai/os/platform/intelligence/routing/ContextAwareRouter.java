package com.shreeai.os.platform.intelligence.routing;

import com.shreeai.os.platform.intelligence.agent.Agent;
import com.shreeai.os.platform.intelligence.agent.AgentCapability;
import com.shreeai.os.platform.intelligence.agent.AgentRegistry;
import com.shreeai.os.platform.runtime.observability.FeatureFlag;
import com.shreeai.os.platform.runtime.observability.FeatureFlags;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * <b>ContextAwareRouter</b>
 *
 * <p>V3: Routes a {@link RoutingContext} to the best-fitting agent from an
 * {@link AgentRegistry}. Routing is gated by the
 * {@link FeatureFlag#CONTEXT_AWARE_ROUTING} flag so it can ship dark and be
 * enabled at runtime.</p>
 *
 * <p><b>Routing strategy:</b></p>
 * <ol>
 *   <li>If the context requires a specific capability, pick the first ACTIVE
 *       agent that declares it (high confidence).</li>
 *   <li>Otherwise, score ACTIVE agents by keyword/name overlap against the
 *       request text and declared capabilities, choosing the best match.</li>
 * </ol>
 *
 * <p><b>Ownership:</b> Intelligence — Context-aware Routing</p>
 * <p><b>Version:</b> 3.0</p>
 *
 * @since 3.0
 */
public final class ContextAwareRouter {

    private static final double EXPLICIT_CONFIDENCE = 0.95;
    private static final double MIN_HEURISTIC_CONFIDENCE = 0.30;
    private static final double CAPABILITY_MATCH_WEIGHT = 0.6;
    private static final double NAME_MATCH_WEIGHT = 0.4;

    private final AgentRegistry agentRegistry;
    private final FeatureFlags featureFlags;

    /**
     * Creates a router over the given agent registry (routing enabled by
     * default).
     *
     * @param agentRegistry the agent registry (never null)
     */
    public ContextAwareRouter(AgentRegistry agentRegistry) {
        this(agentRegistry, new FeatureFlags());
    }

    /**
     * Creates a router with an explicit feature-flag manager.
     *
     * @param agentRegistry the agent registry (never null)
     * @param featureFlags  the feature-flag manager (never null)
     */
    public ContextAwareRouter(AgentRegistry agentRegistry, FeatureFlags featureFlags) {
        this.agentRegistry = Objects.requireNonNull(agentRegistry, "agentRegistry must not be null");
        this.featureFlags = Objects.requireNonNull(featureFlags, "featureFlags must not be null");
    }

    /**
     * Routes a context to the best-fitting agent.
     *
     * @param context the routing context (never null)
     * @return the routing target, or empty when no suitable agent is found
     */
    public Optional<RoutingTarget> route(RoutingContext context) {
        Objects.requireNonNull(context, "context must not be null");

        if (!featureFlags.isEnabled(FeatureFlag.CONTEXT_AWARE_ROUTING)) {
            return Optional.empty();
        }

        List<Agent> candidates = agentRegistry.dispatchable();
        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        if (context.hasRequiredCapability()) {
            return routeByCapability(context, candidates);
        }
        return routeByHeuristics(context, candidates);
    }

    /**
     * @return the underlying agent registry
     */
    public AgentRegistry agentRegistry() {
        return agentRegistry;
    }

    // ==========================================================
    // Routing Strategies
    // ==========================================================

    private Optional<RoutingTarget> routeByCapability(RoutingContext context,
                                                      List<Agent> candidates) {
        AgentCapability required = context.requiredCapability();
        return candidates.stream()
                .filter(a -> a.hasCapability(required))
                .findFirst()
                .map(agent -> RoutingTarget.builder()
                        .agent(agent)
                        .matchedCapability(required)
                        .confidence(EXPLICIT_CONFIDENCE)
                        .reason("explicit capability match: " + required)
                        .build());
    }

    private Optional<RoutingTarget> routeByHeuristics(RoutingContext context,
                                                      List<Agent> candidates) {
        String text = context.requestText().toLowerCase(Locale.ROOT);

        Agent best = null;
        AgentCapability bestCapability = null;
        double bestScore = 0.0;

        for (Agent agent : candidates) {
            AgentCapability matched = null;
            double score = 0.0;

            for (AgentCapability capability : agent.capabilities()) {
                double capScore = capabilityMatches(text, capability) ? 1.0 : 0.0;
                score += capScore * CAPABILITY_MATCH_WEIGHT;
                if (capScore > 0 && matched == null) {
                    matched = capability;
                }
            }
            for (String keyword : context.keywords()) {
                if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                    score += 0.1;
                }
            }
            if (textContains(text, agent.name())) {
                score += NAME_MATCH_WEIGHT;
            }

            if (score > bestScore) {
                bestScore = score;
                best = agent;
                bestCapability = matched;
            }
        }

        if (best == null || bestScore < MIN_HEURISTIC_CONFIDENCE) {
            return Optional.empty();
        }

        final double score = Math.min(1.0, bestScore);
        final AgentCapability cap = bestCapability;
        return Optional.of(RoutingTarget.builder()
                .agent(best)
                .matchedCapability(cap)
                .confidence(score)
                .reason("heuristic match score " + String.format("%.2f", score))
                .build());
    }

    private boolean textContains(String text, String fragment) {
        return fragment != null && text.contains(fragment.toLowerCase(Locale.ROOT));
    }

    /**
     * Matches a capability when the request text contains the full capability
     * name or any of its underscore-separated tokens (e.g. TOOL_EXECUTION
     * matches "tool" or "execution").
     */
    private boolean capabilityMatches(String text, AgentCapability capability) {
        if (textContains(text, capability.name())) {
            return true;
        }
        for (String token : capability.name().toLowerCase(Locale.ROOT).split("_")) {
            if (!token.isEmpty() && text.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
