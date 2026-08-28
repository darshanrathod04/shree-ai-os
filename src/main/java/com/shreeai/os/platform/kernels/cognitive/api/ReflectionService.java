package com.shreeai.os.platform.kernels.cognitive.api;

/**
 * <b>ReflectionService</b>
 *
 * <p>Defines contracts for reflective analysis, including self-analysis,
 * execution review, outcome evaluation, strategy reflection, and
 * improvement recommendations.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines contracts for self-analysis.</li>
 *   <li>Specifies execution review contracts.</li>
 *   <li>Defines outcome evaluation contracts.</li>
 *   <li>Specifies strategy reflection contracts.</li>
 *   <li>Defines improvement recommendation contracts.</li>
 *   <li>Contains no reflection logic — contracts only.</li>
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
 * @see ReasoningService
 * @see DecisionService
 * @see CognitiveStateService
 */
public interface ReflectionService {

    /**
     * Performs self-analysis of cognitive processes.
     *
     * <p>This operation analyzes the cognitive system's own processes, identifying
     * strengths, weaknesses, and areas for improvement.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param analysisScope the scope of self-analysis (must not be {@code null})
     * @return a self-analysis result identifier
     * @throws IllegalArgumentException if analysisScope is {@code null}
     */
    String performSelfAnalysis(AnalysisScope analysisScope);

    /**
     * Reviews execution patterns and outcomes.
     *
     * <p>This operation examines past executions to identify patterns, successes,
     * and failures that can inform future cognitive processes.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param executionContext the execution context to review (must not be {@code null})
     * @return an execution review result identifier
     * @throws IllegalArgumentException if executionContext is {@code null}
     */
    String reviewExecution(ExecutionContext executionContext);

    /**
     * Evaluates outcomes of cognitive processes.
     *
     * <p>This operation assesses the quality and effectiveness of cognitive
     * process outcomes against defined objectives.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param outcome the outcome to evaluate (must not be {@code null})
     * @param evaluationCriteria the criteria for evaluation (must not be {@code null})
     * @return an outcome evaluation result identifier
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    String evaluateOutcome(Outcome outcome, EvaluationCriteria evaluationCriteria);

    /**
     * Reflects on cognitive strategies.
     *
     * <p>This operation analyzes the effectiveness of cognitive strategies and
     * identifies opportunities for strategic improvement.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param strategy the strategy to reflect upon (must not be {@code null})
     * @param reflectionDepth the depth of reflection to perform (must not be {@code null})
     * @return a strategy reflection result identifier
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    String reflectOnStrategy(Strategy strategy, ReflectionDepth reflectionDepth);

    /**
     * Generates improvement recommendations.
     *
     * <p>This operation analyzes cognitive performance and generates actionable
     * recommendations for improvement.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param improvementContext the context for improvement (must not be {@code null})
     * @return an improvement recommendation result identifier
     * @throws IllegalArgumentException if improvementContext is {@code null}
     */
    String generateImprovementRecommendation(ImprovementContext improvementContext);
}