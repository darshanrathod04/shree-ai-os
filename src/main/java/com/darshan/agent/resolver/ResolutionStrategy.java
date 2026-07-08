package com.darshan.agent.resolver;

/**
 * Defines the strategy used to resolve which capability should handle a request.
 * <p>
 * Strategies are extensible — new strategies can be added without breaking
 * existing resolution logic. Each strategy represents a distinct resolution
 * approach.
 * <p>
 * Current strategies:
 * <ul>
 *   <li>{@link #DIRECT_MATCH} — Intent directly matches a capability</li>
 *   <li>{@link #PRIORITY_SELECTION} — Multiple candidates resolved by priority</li>
 *   <li>{@link #CONTEXT_SELECTION} — Context-aware selection (e.g., active course)</li>
 *   <li>{@link #FALLBACK} — Default fallback when no match found</li>
 *   <li>{@link #DEFAULT} — Default capability (Chat) selected</li>
 *   <li>{@link #UNKNOWN} — No resolution possible</li>
 * </ul>
 * <p>
 * Future strategies (pre-defined for extension):
 * <ul>
 *   <li>AI_ASSISTED — LLM-based capability selection</li>
 *   <li>MULTI_CAPABILITY — Composite resolution for multi-intent queries</li>
 * </ul>
 */
public enum ResolutionStrategy {

    /**
     * Intent directly matches a single capability's supported intents.
     * Highest confidence resolution.
     */
    DIRECT_MATCH,

    /**
     * Multiple capabilities support the intent; highest priority wins.
     */
    PRIORITY_SELECTION,

    /**
     * Context (e.g., active course, session state) determines selection.
     */
    CONTEXT_SELECTION,

    /**
     * No capability supports the intent; fallback to default handler.
     */
    FALLBACK,

    /**
     * Default capability selected (typically ChatCapability).
     */
    DEFAULT,

    /**
     * No resolution possible — no matching capability found.
     */
    UNKNOWN,

    // ── Future Extension Points ──

    /**
     * AI/Learning-assisted capability selection.
     * Reserved for future use.
     */
    AI_ASSISTED,

    /**
     * Composite resolution for queries spanning multiple intents.
     * Reserved for future use.
     */
    MULTI_CAPABILITY
}