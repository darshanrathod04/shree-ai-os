package com.shreeai.os.platform.kernels.cognitive.validation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.shreeai.os.platform.kernels.cognitive.model.CognitiveState;
import com.shreeai.os.platform.kernels.cognitive.model.DecisionContext;
import com.shreeai.os.platform.kernels.cognitive.model.EvaluationCriteria;
import com.shreeai.os.platform.kernels.cognitive.model.Hypothesis;
import com.shreeai.os.platform.kernels.cognitive.model.ReasoningRequest;
import com.shreeai.os.platform.kernels.cognitive.model.ReflectionScope;

/**
 * <b>CognitiveValidator</b>
 *
 * <p>Acts as the entry point for Cognitive domain model validation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Coordinates specialized validators for cognitive domain models.</li>
 *   <li>Aggregates validation results from all validators.</li>
 *   <li>Exposes a unified validation interface for the Cognitive Kernel.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Cognitive Kernel - Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This class is stateless, deterministic, thread-safe, and read-only.
 * It maintains no mutable fields and performs only structural validation.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-COG-103, EIO-ARCH-001</p>
 *
 * <p><b>Validation Scope:</b></p>
 * <p>This validator performs structural validation only. It verifies that cognitive domain models
 * are well-formed and satisfy their construction invariants. It does not determine correctness,
 * truth, or quality of reasoning.</p>
 *
 * @since 1.0
 */
public final class CognitiveValidator {

    /**
     * Private constructor to prevent instantiation.
     *
     * <p>This class provides only static validation methods and should not be instantiated.</p>
     */
    private CognitiveValidator() {
        // Prevent instantiation
    }

    /**
     * Validates a CognitiveState instance for structural integrity.
     *
     * <p>Performs comprehensive structural validation including identifier presence,
     * mandatory fields, lifecycle consistency, immutable collections, and constructor invariants.</p>
     *
     * <p><b>Validation Responsibilities:</b></p>
     * <ul>
     *   <li>Identifier presence and format</li>
     *   <li>Mandatory field validation</li>
     *   <li>Lifecycle consistency</li>
     *   <li>Immutable collection integrity</li>
     *   <li>Constructor invariants</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not evaluate reasoning quality</li>
     *   <li>Does not determine state correctness</li>
     *   <li>Does not assess cognitive performance</li>
     * </ul>
     *
     * @param state the CognitiveState to validate (must not be {@code null})
     * @return an immutable validation result
     * @throws IllegalArgumentException if state is {@code null}
     */
    public static CognitiveValidationResult validateCognitiveState(CognitiveState state) {
        Objects.requireNonNull(state, "CognitiveState must not be null for validation");
        return CognitiveStateValidator.validate(state);
    }

    /**
     * Validates a ReasoningRequest instance for structural integrity.
     *
     * <p>Performs comprehensive structural validation including request identifier,
     * required inputs, constraints, metadata structure, and constructor invariants.</p>
     *
     * <p><b>Validation Responsibilities:</b></p>
     * <ul>
     *   <li>Request identifier presence</li>
     *   <li>Required inputs validation</li>
     *   <li>Constraints structure</li>
     *   <li>Metadata structure</li>
     *   <li>Constructor invariants</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not evaluate reasoning quality</li>
     *   <li>Does not determine request validity</li>
     *   <li>Does not assess reasoning outcomes</li>
     * </ul>
     *
     * @param request the ReasoningRequest to validate (must not be {@code null})
     * @return an immutable validation result
     * @throws IllegalArgumentException if request is {@code null}
     */
    public static CognitiveValidationResult validateReasoningRequest(ReasoningRequest request) {
        Objects.requireNonNull(request, "ReasoningRequest must not be null for validation");
        return ReasoningRequestValidator.validate(request);
    }

    /**
     * Validates a DecisionContext instance for structural integrity.
     *
     * <p>Performs comprehensive structural validation including identifier,
     * alternatives collection, assumptions, constraints, and metadata.</p>
     *
     * <p><b>Validation Responsibilities:</b></p>
     * <ul>
     *   <li>Identifier presence</li>
     *   <li>Alternatives collection structure</li>
     *   <li>Assumptions structure</li>
     *   <li>Constraints structure</li>
     *   <li>Metadata integrity</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not compare alternatives</li>
     *   <li>Does not evaluate decision quality</li>
     *   <li>Does not rank or score decisions</li>
     * </ul>
     *
     * @param context the DecisionContext to validate (must not be {@code null})
     * @return an immutable validation result
     * @throws IllegalArgumentException if context is {@code null}
     */
    public static CognitiveValidationResult validateDecisionContext(DecisionContext context) {
        Objects.requireNonNull(context, "DecisionContext must not be null for validation");
        return DecisionContextValidator.validate(context);
    }

    /**
     * Validates a ReflectionScope instance for structural integrity.
     *
     * <p>Performs comprehensive structural validation including scope definition,
     * target presence, boundary consistency, and metadata integrity.</p>
     *
     * <p><b>Validation Responsibilities:</b></p>
     * <ul>
     *   <li>Scope definition validation</li>
     *   <li>Target presence</li>
     *   <li>Boundary consistency</li>
     *   <li>Metadata integrity</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not perform reflective analysis</li>
     *   <li>Does not evaluate reflection outcomes</li>
     *   <li>Does not assess reflection quality</li>
     * </ul>
     *
     * @param scope the ReflectionScope to validate (must not be {@code null})
     * @return an immutable validation result
     * @throws IllegalArgumentException if scope is {@code null}
     */
    public static CognitiveValidationResult validateReflectionScope(ReflectionScope scope) {
        Objects.requireNonNull(scope, "ReflectionScope must not be null for validation");
        return ReflectionScopeValidator.validate(scope);
    }

    /**
     * Validates an EvaluationCriteria instance for structural integrity.
     *
     * <p>Performs comprehensive structural validation including criterion definitions,
     * weights, priorities, and metadata.</p>
     *
     * <p><b>Validation Responsibilities:</b></p>
     * <ul>
     *   <li>Criterion definitions validation</li>
     *   <li>Weights validation</li>
     *   <li>Priorities validation</li>
     *   <li>Metadata integrity</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not score or rank criteria</li>
     *   <li>Does not evaluate criteria quality</li>
     *   <li>Does not determine criteria effectiveness</li>
     * </ul>
     *
     * @param criteria the EvaluationCriteria to validate (must not be {@code null})
     * @return an immutable validation result
     * @throws IllegalArgumentException if criteria is {@code null})
     */
    public static CognitiveValidationResult validateEvaluationCriteria(EvaluationCriteria criteria) {
        Objects.requireNonNull(criteria, "EvaluationCriteria must not be null for validation");
        return EvaluationCriteriaValidator.validate(criteria);
    }

    /**
     * Validates a Hypothesis instance for structural integrity.
     *
     * <p>Performs comprehensive structural validation including hypothesis identifier,
     * statement presence, assumption structure, evidence references, and metadata.</p>
     *
     * <p><b>Validation Responsibilities:</b></p>
     * <ul>
     *   <li>Hypothesis identifier presence</li>
     *   <li>Statement presence</li>
     *   <li>Assumption structure</li>
     *   <li>Evidence references</li>
     *   <li>Metadata integrity</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not determine whether a hypothesis is true</li>
     *   <li>Does not evaluate hypothesis quality</li>
     *   <li>Does not assess evidence validity</li>
     * </ul>
     *
     * @param hypothesis the Hypothesis to validate (must not be {@code null})
     * @return an immutable validation result
     * @throws IllegalArgumentException if hypothesis is {@code null})
     */
    public static CognitiveValidationResult validateHypothesis(Hypothesis hypothesis) {
        Objects.requireNonNull(hypothesis, "Hypothesis must not be null for validation");
        return HypothesisValidator.validate(hypothesis);
    }

    /**
     * Validates all cognitive domain models and aggregates the results.
     *
     * <p>Validates each model independently and combines all violations into a single result.
     * This method is useful for comprehensive validation of complete cognitive structures.</p>
     *
     * <p><b>Note:</b> This method validates structural integrity only. It does not evaluate
     * reasoning quality, decision correctness, or hypothesis validity.</p>
     *
     * @param state the CognitiveState to validate (may be {@code null})
     * @param request the ReasoningRequest to validate (may be {@code null})
     * @param context the DecisionContext to validate (may be {@code null})
     * @param scope the ReflectionScope to validate (may be {@code null})
     * @param criteria the EvaluationCriteria to validate (may be {@code null})
     * @param hypothesis the Hypothesis to validate (may be {@code null})
     * @return an aggregated immutable validation result
     */
    public static CognitiveValidationResult validateAll(
            CognitiveState state,
            ReasoningRequest request,
            DecisionContext context,
            ReflectionScope scope,
            EvaluationCriteria criteria,
            Hypothesis hypothesis) {

        List<String> allViolations = new ArrayList<>();
        Instant validatedAt = Instant.now();
        Map<String, Object> metadata = new HashMap<>();

        if (state != null) {
            CognitiveValidationResult result = validateCognitiveState(state);
            if (!result.valid()) {
                allViolations.addAll(result.violations());
            }
        }

        if (request != null) {
            CognitiveValidationResult result = validateReasoningRequest(request);
            if (!result.valid()) {
                allViolations.addAll(result.violations());
            }
        }

        if (context != null) {
            CognitiveValidationResult result = validateDecisionContext(context);
            if (!result.valid()) {
                allViolations.addAll(result.violations());
            }
        }

        if (scope != null) {
            CognitiveValidationResult result = validateReflectionScope(scope);
            if (!result.valid()) {
                allViolations.addAll(result.violations());
            }
        }

        if (criteria != null) {
            CognitiveValidationResult result = validateEvaluationCriteria(criteria);
            if (!result.valid()) {
                allViolations.addAll(result.violations());
            }
        }

        if (hypothesis != null) {
            CognitiveValidationResult result = validateHypothesis(hypothesis);
            if (!result.valid()) {
                allViolations.addAll(result.violations());
            }
        }

        metadata.put("validatedModels", new HashMap<String, Object>() {{
            put("cognitiveState", state != null);
            put("reasoningRequest", request != null);
            put("decisionContext", context != null);
            put("reflectionScope", scope != null);
            put("evaluationCriteria", criteria != null);
            put("hypothesis", hypothesis != null);
        }});

        return new CognitiveValidationResult(
                allViolations.isEmpty(),
                allViolations,
                validatedAt,
                metadata
        );
    }
}