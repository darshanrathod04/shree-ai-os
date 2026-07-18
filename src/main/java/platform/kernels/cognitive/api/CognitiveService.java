package platform.kernels.cognitive.api;

/**
 * <b>CognitiveService</b>
 *
 * <p>Primary façade for the Cognitive Kernel, providing high-level cognitive
 * operations and delegating specialized responsibilities to subordinate service
 * contracts.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines high-level cognitive operations for the platform.</li>
 *   <li>Delegates specialized cognitive tasks to subordinate services.</li>
 *   <li>Maintains technology-agnostic business-level contracts.</li>
 *   <li>Exposes the primary entry point for cognitive capabilities.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Business-focused — exposes only business-level contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-101, EIO-ARCH-001</p>
 *
 * @see ReasoningService
 * @see DecisionService
 * @see ReflectionService
 * @see CognitiveStateService
 */
public interface CognitiveService {

    /**
     * Performs comprehensive cognitive analysis over knowledge.
     *
     * <p>This operation coordinates reasoning, decision support, and reflection
     * to provide a holistic cognitive analysis of the specified knowledge context.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param knowledgeContextId the identifier of the knowledge context to analyze
     *                           (must not be {@code null} or empty)
     * @param analysisDepth      the depth of analysis to perform (must not be {@code null})
     * @return a cognitive analysis result identifier
     * @throws IllegalArgumentException if any parameter is invalid
     */
    String analyzeKnowledge(String knowledgeContextId, AnalysisDepth analysisDepth);

    /**
     * Initiates a cognitive reasoning session.
     *
     * <p>This operation creates a reasoning session that can be used to perform
     * logical analysis, inference, and hypothesis evaluation over knowledge.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param reasoningRequest the reasoning request parameters (must not be {@code null})
     * @return a reasoning session identifier
     * @throws IllegalArgumentException if reasoningRequest is {@code null}
     */
    String initiateReasoning(ReasoningRequest reasoningRequest);

    /**
     * Supports decision-making processes.
     *
     * <p>This operation evaluates alternatives, performs trade-off analysis, and
     * generates recommendations to support decision-making.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param decisionContext the decision context (must not be {@code null})
     * @return a decision support result identifier
     * @throws IllegalArgumentException if decisionContext is {@code null}
     */
    String supportDecision(DecisionContext decisionContext);

    /**
     * Performs reflective analysis on cognitive processes.
     *
     * <p>This operation analyzes execution patterns, evaluates outcomes, and
     * generates improvement recommendations.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param reflectionScope the scope of reflection (must not be {@code null})
     * @return a reflection result identifier
     * @throws IllegalArgumentException if reflectionScope is {@code null}
     */
    String performReflection(ReflectionScope reflectionScope);

    /**
     * Retrieves the current cognitive state.
     *
     * <p>This operation returns the current state of cognitive processes including
     * attention, focus, and reasoning lifecycle.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param stateQuery the state query parameters (must not be {@code null})
     * @return the current cognitive state
     * @throws IllegalArgumentException if stateQuery is {@code null}
     */
    CognitiveState getCognitiveState(StateQuery stateQuery);

    /**
     * Defines the depth of cognitive analysis.
     */
    enum AnalysisDepth {
        /**
         * Surface-level analysis focusing on immediate patterns.
         */
        SURFACE,

        /**
         * Standard analysis with moderate depth.
         */
        STANDARD,

        /**
         * Deep analysis with comprehensive reasoning.
         */
        DEEP,

        /**
         * Exhaustive analysis with full cognitive processing.
         */
        EXHAUSTIVE
    }
}