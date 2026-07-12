/**
 * Capability Resolver — Shadow Mode.
 *
 * <h2>Purpose</h2>
 * The Capability Resolver determines which registered capability
 * SHOULD handle the current request. It does NOT execute anything,
 * does NOT replace switch(intent), and does NOT change production behavior.
 *
 * <h2>Architecture Flow</h2>
 * <pre>
 * User → UQC → IntentEngine → CapabilityRegistry → CapabilityResolver (Shadow) → switch(intent) → Handler
 * </pre>
 *
 * <h2>Key Principles</h2>
 * <ul>
 *   <li><b>Shadow Mode Only</b> — The resolver observes but never affects execution.</li>
 *   <li><b>No Execution</b> — The resolver answers "which capability SHOULD handle this?" only.</li>
 *   <li><b>Deterministic</b> — All scoring is based on defined weights, no randomness or LLM.</li>
 *   <li><b>Immutable Results</b> — {@link platform.resolver.CapabilityResolution} is fully immutable.</li>
 *   <li><b>Thread-Safe</b> — No mutable static state, constructor injection only.</li>
 * </ul>
 *
 * <h2>Components</h2>
 * <ul>
 *   <li>{@link platform.resolver.CapabilityResolver} — Main entry point. Resolves intent to capability.</li>
 *   <li>{@link platform.resolver.CapabilityResolution} — Immutable resolution result with full metadata.</li>
 *   <li>{@link platform.resolver.CapabilityScorer} — Deterministic scoring engine (40% intent, 20% priority, 20% context, 10% health, 10% availability).</li>
 *   <li>{@link platform.resolver.ResolutionStrategy} — Extensible enum of resolution strategies.</li>
 * </ul>
 *
 * <h2>Non-Responsibilities</h2>
 * <ul>
 *   <li>Does NOT execute capabilities</li>
 *   <li>Does NOT replace AgentBrain switch(intent) routing</li>
 *   <li>Does NOT rewrite CapabilityRegistry</li>
 *   <li>Does NOT call Ollama or any external service</li>
 *   <li>Does NOT duplicate business logic</li>
 *   <li>Does NOT break existing APIs or tests</li>
 * </ul>
 *
 * <h2>Future Evolution</h2>
 * <ul>
 *   <li><b>AI_ASSISTED</b> — LLM-based capability selection (pre-defined strategy)</li>
 *   <li><b>MULTI_CAPABILITY</b> — Composite resolution for multi-intent queries</li>
 *   <li><b>Context Boost</b> — Enhanced context-aware scoring with session history</li>
 *   <li><b>Health-Based Re-routing</b> — Automatic fallback if preferred capability is unhealthy</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * All classes in this package are thread-safe:
 * <ul>
 *   <li>CapabilityResolver — no mutable state, final field only</li>
 *   <li>CapabilityScorer — stateless utility class</li>
 *   <li>CapabilityResolution — fully immutable, all fields final</li>
 *   <li>ResolutionStrategy — enum (singleton safe)</li>
 * </ul>
 *
 * @since 1.0.0
 * @see platform.resolver.CapabilityResolver
 * @see platform.resolver.CapabilityResolution
 * @see platform.resolver.CapabilityScorer
 */
package platform.resolver;