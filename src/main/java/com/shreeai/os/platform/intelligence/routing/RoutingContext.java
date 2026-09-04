package com.shreeai.os.platform.intelligence.routing;

import com.shreeai.os.platform.intelligence.agent.AgentCapability;

import java.util.Set;

/**
 * <b>RoutingContext</b>
 *
 * <p>Carries the features used by {@link ContextAwareRouter} to decide which
 * agent should handle a request: the request text, an optional inferred
 * required capability, and an optional set of keyword hints.</p>
 *
 * <p><b>Ownership:</b> Intelligence — Context-aware Routing</p>
 * <p><b>Version:</b> 3.0</p>
 *
 * @since 3.0
 */
public record RoutingContext(
        String requestText,
        AgentCapability requiredCapability,
        Set<String> keywords) {

    /**
     * Creates a RoutingContext with normalisation.
     *
     * @throws IllegalArgumentException if requestText is null
     */
    public RoutingContext {
        if (requestText == null) {
            throw new IllegalArgumentException("requestText must not be null");
        }
        keywords = keywords == null ? Set.of() : Set.copyOf(keywords);
    }

    /** @return the request text (never null) */
    @Override
    public String requestText() {
        return requestText;
    }

    /**
     * @return true when a specific capability is required for routing
     */
    public boolean hasRequiredCapability() {
        return requiredCapability != null;
    }

    /**
     * Creates a context with only request text.
     *
     * @param requestText the request text
     * @return a new context
     */
    public static RoutingContext of(String requestText) {
        return new RoutingContext(requestText, null, Set.of());
    }

    /**
     * Creates a context with a required capability.
     *
     * @param requestText         the request text
     * @param requiredCapability  the required capability (may be null)
     * @return a new context
     */
    public static RoutingContext forCapability(String requestText,
                                               AgentCapability requiredCapability) {
        return new RoutingContext(requestText, requiredCapability, Set.of());
    }
}
