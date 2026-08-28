package com.shreeai.os.platform.kernels.cognitive.api;

/**
 * <b>DecisionService</b>
 *
 * <p>Defines contracts for decision support, including decision generation,
 * alternative evaluation, trade-off analysis, recommendation generation,
 * and decision confidence.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines contracts for decision generation.</li>
 *   <li>Specifies alternative evaluation contracts.</li>
 *   <li>Defines trade-off analysis contracts.</li>
 *   <li>Specifies recommendation generation contracts.</li>
 *   <li>Defines decision confidence contracts.</li>
 *   <li>Contains no decision logic — contracts only.</li>
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
 * @see ReflectionService
 * @see CognitiveStateService
 */
public interface DecisionService {

    /**
     * Generates a decision based on the provided context.
     *
     * <p>This operation evaluates the decision context and generates a decision
     * that can be used to guide platform behavior.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param decisionContext the decision context (must not be {@code null})
     * @return a decision result identifier
     * @throws IllegalArgumentException if decisionContext is {@code null}
     */
    String generateDecision(DecisionContext decisionContext);

    /**
     * Evaluates alternatives for a decision.
     *
     * <p>This operation assesses multiple alternatives and provides evaluation
     * metrics for each option.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param alternatives the alternatives to evaluate (must not be {@code null})
     * @param evaluationCriteria the criteria for evaluation (must not be {@code null})
     * @return an alternative evaluation result identifier
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    String evaluateAlternatives(Alternatives alternatives, EvaluationCriteria evaluationCriteria);

    /**
     * Performs trade-off analysis for a decision.
     *
     * <p>This operation analyzes the trade-offs between different decision options,
     * identifying pros, cons, and opportunity costs.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param decisionOptions the decision options to analyze (must not be {@code null})
     * @param tradeOffCriteria the criteria for trade-off analysis (must not be {@code null})
     * @return a trade-off analysis result identifier
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    String analyzeTradeOffs(DecisionOptions decisionOptions, TradeOffCriteria tradeOffCriteria);

    /**
     * Generates recommendations for a decision.
     *
     * <p>This operation generates ranked recommendations based on the decision
     * context and evaluation criteria.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param recommendationRequest the recommendation request parameters (must not be {@code null})
     * @return a recommendation result identifier
     * @throws IllegalArgumentException if recommendationRequest is {@code null}
     */
    String generateRecommendation(RecommendationRequest recommendationRequest);

    /**
     * Evaluates the confidence level of a decision.
     *
     * <p>This operation assesses the confidence level of a decision based on
     * available evidence and reasoning quality.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param decisionId the identifier of the decision to evaluate (must not be {@code null} or empty)
     * @return a decision confidence evaluation result identifier
     * @throws IllegalArgumentException if decisionId is {@code null} or empty
     */
    String evaluateConfidence(String decisionId);
}