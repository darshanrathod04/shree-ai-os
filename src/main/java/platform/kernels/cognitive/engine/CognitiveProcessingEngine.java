package platform.kernels.cognitive.engine;

import java.util.Map;

import platform.kernels.cognitive.model.CognitiveState;
import platform.kernels.cognitive.model.DecisionContext;
import platform.kernels.cognitive.model.EvaluationCriteria;
import platform.kernels.cognitive.model.Hypothesis;
import platform.kernels.cognitive.model.ReasoningRequest;
import platform.kernels.cognitive.model.ReflectionScope;

/**
 * <b>CognitiveProcessingEngine</b>
 *
 * <p>Processing contract for deterministic cognitive operations.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines processing contracts for cognitive operations</li>
 *   <li>Delegates cognitive computation to specialized engines</li>
 *   <li>Maintains no implementation - interface only</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel - Engine Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an interface with no implementation. It defines
 * processing contracts only. Actual processing logic is implemented by
 * DefaultCognitiveProcessingEngine.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-106, EIO-ARCH-001</p>
 *
 * <p><b>Processing Responsibilities:</b></p>
 * <p>The engine performs deterministic computation only. It transforms validated
 * cognitive inputs into immutable processing results without introducing adaptive
 * behavior, learning, orchestration, or autonomous decision-making.</p>
 *
 * @param <T> the type of processing result
 * @since 1.0
 */
public interface CognitiveProcessingEngine<T> {

    /**
     * Processes a reasoning request.
     *
     * <p>Executes deterministic reasoning computation on the validated request.
     * This method performs structural processing and transformation only.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Execute deterministic reasoning operations</li>
     *   <li>Transform validated inputs</li>
     *   <li>Generate immutable processing results</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs (validation is handled by the service layer)</li>
     *   <li>Does not translate exceptions (exception translation is handled by the service layer)</li>
     *   <li>Does not perform adaptive learning or probabilistic reasoning</li>
     * </ul>
     *
     * @param request the reasoning request to process (must not be {@code null})
     * @param context the cognitive state context (must not be {@code null})
     * @return the processing result
     */
    T processReasoning(ReasoningRequest request, CognitiveState context);

    /**
     * Processes an inference request.
     *
     * <p>Executes deterministic inference computation on the validated inputs.
     * This method performs structural processing and transformation only.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Execute deterministic inference operations</li>
     *   <li>Transform validated inputs</li>
     *   <li>Generate immutable processing results</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs (validation is handled by the service layer)</li>
     *   <li>Does not translate exceptions (exception translation is handled by the service layer)</li>
     *   <li>Does not perform autonomous planning</li>
     * </ul>
     *
     * @param request the reasoning request (must not be {@code null})
     * @param state the cognitive state (must not be {@code null})
     * @return the processing result
     */
    T processInference(ReasoningRequest request, CognitiveState state);

    /**
     * Processes a decision request.
     *
     * <p>Executes deterministic decision computation on the validated inputs.
     * This method performs structural processing and transformation only.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Execute deterministic decision support operations</li>
     *   <li>Transform validated inputs</li>
     *   <li>Generate immutable processing results</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs (validation is handled by the service layer)</li>
     *   <li>Does not translate exceptions (exception translation is handled by the service layer)</li>
     *   <li>Does not execute decision algorithms or rank alternatives</li>
     * </ul>
     *
     * @param context the decision context (must not be {@code null})
     * @param criteria the evaluation criteria (must not be {@code null})
     * @param state the cognitive state (must not be {@code null})
     * @return the processing result
     */
    T processDecision(DecisionContext context, EvaluationCriteria criteria, CognitiveState state);

    /**
     * Processes a reflection request.
     *
     * <p>Executes deterministic reflection computation on the validated inputs.
     * This method performs structural processing and transformation only.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Execute deterministic reflective analysis operations</li>
     *   <li>Transform validated inputs</li>
     *   <li>Generate immutable processing results</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs (validation is handled by the service layer)</li>
     *   <li>Does not translate exceptions (exception translation is handled by the service layer)</li>
     *   <li>Does not perform reflective analysis or assess quality</li>
     * </ul>
     *
     * @param scope the reflection scope (must not be {@code null})
     * @param state the cognitive state (must not be {@code null})
     * @return the processing result
     */
    T processReflection(ReflectionScope scope, CognitiveState state);

    /**
     * Processes a cognitive state operation.
     *
     * <p>Executes deterministic state computation on the validated inputs.
     * This method performs structural processing and transformation only.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Execute deterministic state operations</li>
     *   <li>Transform validated inputs</li>
     *   <li>Generate immutable processing results</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs (validation is handled by the service layer)</li>
     *   <li>Does not translate exceptions (exception translation is handled by the service layer)</li>
     *   <li>Does not modify state directly or evaluate correctness</li>
     * </ul>
     *
     * @param currentState the current cognitive state (must not be {@code null})
     * @param transition the state transition data (must not be {@code null})
     * @return the processing result
     */
    T processCognitiveState(CognitiveState currentState, Map<String, Object> transition);

    /**
     * Processes a recommendation request.
     *
     * <p>Executes deterministic recommendation computation on the validated inputs.
     * This method performs structural processing and transformation only.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Execute deterministic recommendation operations</li>
     *   <li>Transform validated inputs</li>
     *   <li>Generate immutable processing results</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs (validation is handled by the service layer)</li>
     *   <li>Does not translate exceptions (exception translation is handled by the service layer)</li>
     *   <li>Does not rank or score recommendations</li>
     * </ul>
     *
     * @param context the decision context (must not be {@code null})
     * @param criteria the evaluation criteria (must not be {@code null})
     * @param state the cognitive state (must not be {@code null})
     * @return the processing result
     */
    T processRecommendation(DecisionContext context, EvaluationCriteria criteria, CognitiveState state);

    /**
     * Processes an analysis request.
     *
     * <p>Executes deterministic analysis computation on the validated inputs.
     * This method performs structural processing and transformation only.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Execute deterministic analysis operations</li>
     *   <li>Transform validated inputs</li>
     *   <li>Generate immutable processing results</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs (validation is handled by the service layer)</li>
     *   <li>Does not translate exceptions (exception translation is handled by the service layer)</li>
     *   <li>Does not perform workflow execution or orchestration</li>
     * </ul>
     *
     * @param hypothesis the hypothesis to analyze (must not be {@code null})
     * @param state the cognitive state (must not be {@code null})
     * @return the processing result
     */
    T processAnalysis(Hypothesis hypothesis, CognitiveState state);

    /**
     * Processes an evaluation request.
     *
     * <p>Executes deterministic evaluation computation on the validated inputs.
     * This method performs structural processing and transformation only.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Execute deterministic evaluation operations</li>
     *   <li>Transform validated inputs</li>
     *   <li>Generate immutable processing results</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs (validation is handled by the service layer)</li>
     *   <li>Does not translate exceptions (exception translation is handled by the service layer)</li>
     *   <li>Does not score or rank criteria</li>
     * </ul>
     *
     * @param criteria the evaluation criteria (must not be {@code null})
     * @param state the cognitive state (must not be {@code null})
     * @return the processing result
     */
    T processEvaluation(EvaluationCriteria criteria, CognitiveState state);
}