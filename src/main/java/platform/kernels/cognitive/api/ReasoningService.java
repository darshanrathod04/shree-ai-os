package platform.kernels.cognitive.api;

/**
 * <b>ReasoningService</b>
 *
 * <p>Defines contracts for reasoning over the Knowledge Kernel, including
 * reasoning requests, inference operations, hypothesis evaluation, logical
 * analysis, and consistency evaluation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines contracts for reasoning over knowledge.</li>
 *   <li>Specifies inference operation contracts.</li>
 *   <li>Defines hypothesis evaluation contracts.</li>
 *   <li>Specifies logical analysis contracts.</li>
 *   <li>Defines consistency evaluation contracts.</li>
 *   <li>Contains no reasoning logic — contracts only.</li>
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
 * @see CognitiveService
 * @see DecisionService
 * @see ReflectionService
 * @see CognitiveStateService
 */
public interface ReasoningService {

    /**
     * Performs reasoning over knowledge.
     *
     * <p>This operation applies logical reasoning to the specified knowledge context,
     * deriving conclusions and identifying implications.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param reasoningRequest the reasoning request parameters (must not be {@code null})
     * @return a reasoning result identifier
     * @throws IllegalArgumentException if reasoningRequest is {@code null}
     */
    String reason(ReasoningRequest reasoningRequest);

    /**
     * Performs inference over knowledge.
     *
     * <p>This operation derives new knowledge from existing knowledge through
     * logical inference rules.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param inferenceRequest the inference request parameters (must not be {@code null})
     * @return an inference result identifier
     * @throws IllegalArgumentException if inferenceRequest is {@code null}
     */
    String infer(InferenceRequest inferenceRequest);

    /**
     * Evaluates a hypothesis against knowledge.
     *
     * <p>This operation assesses the validity and support for a hypothesis
     * based on available knowledge.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param hypothesis the hypothesis to evaluate (must not be {@code null})
     * @param evaluationCriteria the criteria for evaluation (must not be {@code null})
     * @return a hypothesis evaluation result identifier
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    String evaluateHypothesis(Hypothesis hypothesis, EvaluationCriteria evaluationCriteria);

    /**
     * Performs logical analysis on knowledge.
     *
     * <p>This operation applies formal logical analysis to identify patterns,
     * contradictions, and logical implications.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param analysisRequest the analysis request parameters (must not be {@code null})
     * @return a logical analysis result identifier
     * @throws IllegalArgumentException if analysisRequest is {@code null}
     */
    String analyzeLogically(LogicalAnalysisRequest analysisRequest);

    /**
     * Evaluates the consistency of knowledge.
     *
     * <p>This operation assesses whether knowledge is internally consistent
     * and identifies any contradictions.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param knowledgeContextId the identifier of the knowledge context to evaluate
     *                           (must not be {@code null} or empty)
     * @return a consistency evaluation result identifier
     * @throws IllegalArgumentException if knowledgeContextId is {@code null} or empty
     */
    String evaluateConsistency(String knowledgeContextId);
}