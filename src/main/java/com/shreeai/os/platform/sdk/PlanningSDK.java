package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.kernels.planning.api.PlanningService;
import com.shreeai.os.platform.kernels.planning.api.PlanningTypes;
import com.shreeai.os.platform.kernels.planning.model.PlanningConstraints;
import com.shreeai.os.platform.kernels.planning.model.ValidationCriteria;
import com.shreeai.os.platform.runtime.api.Runtime;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Planning SDK Facade
 *
 * <p>Developer-facing entry point for the Planning Kernel. Provides a
 * non-breaking API to create, refine, and validate plans.</p>
 *
 * <p><b>Runtime path (Sprint-Release-6):</b>
 * {@code PlanningSDK → Runtime.planningService()
 *     → DefaultPlanningService → PlanningProcessingEngine
 *     → PlanningIntelligenceEngine}.</p>
 *
 * <p>The typed methods below delegate to the live {@link PlanningService}
 * when a {@link Runtime} is available and falls back to the legacy
 * string-routing path otherwise. Backward compatibility is preserved:
 * existing callers using {@code createPlan}, {@code refinePlan}, and
 * {@code validatePlan} continue to work unchanged.</p>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class PlanningSDK {

    private final ShreeClient client;
    private final Runtime runtime;

    PlanningSDK(ShreeClient client) {
        this(client, client != null ? client.runtime() : null);
    }

    PlanningSDK(ShreeClient client, Runtime runtime) {
        this.client = Objects.requireNonNull(client, "client must not be null");
        this.runtime = runtime;
    }

    /**
     * Create a new execution plan.
     */
    public SDKResponse createPlan(
            String objectiveId,
            String objective,
            String scope
    ) {

        SDKRequest request = SDKRequest.builder()
                .message("PLANNING_CREATE")
                .metadata(Map.of(
                        "operation", "CREATE_PLAN",
                        "objectiveId", objectiveId,
                        "objective", objective,
                        "scope", scope
                ))
                .build();

        return client.chat(request);
    }

    /**
     * Refine an existing plan.
     */
    public SDKResponse refinePlan(
            String planId,
            String refinement
    ) {

        SDKRequest request = SDKRequest.builder()
                .message("PLANNING_REFINE")
                .metadata(Map.of(
                        "operation", "REFINE_PLAN",
                        "planId", planId,
                        "refinement", refinement
                ))
                .build();

        return client.chat(request);
    }

    /**
     * Validate a plan.
     */
    public SDKResponse validatePlan(String planId) {

        SDKRequest request = SDKRequest.builder()
                .message("PLANNING_VALIDATE")
                .metadata(Map.of(
                        "operation", "VALIDATE_PLAN",
                        "planId", planId
                ))
                .build();

        return client.chat(request);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Sprint-Release-6: Advanced Planning APIs
    //
    // Typed methods that delegate directly to Runtime.planningService() when
    // available. These methods do not invent new features — they expose the
    // capabilities that already exist in DefaultPlanningService /
    // PlanningIntelligenceEngine through a clean public surface.
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Creates a comprehensive plan from a cognitive intent.
     *
     * <p>When a {@link Runtime} is available, this method delegates directly
     * to {@link PlanningService#createPlan(PlanningService.PlanningRequest)}
     * using the typed PlanningRequest record. Otherwise it falls back to the
     * legacy string-routing path ({@link #createPlan(String, String, String)}).</p>
     *
     * <p><b>Runtime path:</b>
     * {@code PlanningSDK → Runtime.planningService().createPlan(request)
     *   → DefaultPlanningService → PlanningProcessingEngine
     *   → PlanningIntelligenceEngine}.</p>
     *
     * @param objectiveId the planning objective identifier (must not be null/blank)
     * @param objective the human-readable objective description
     * @param scope the planning scope (never null)
     * @param constraints the planning constraints (may be null — defaults to empty)
     * @return a structured SDKResponse with the new plan identifier
     * @throws IllegalArgumentException if objectiveId, objective, or scope is null/blank
     */
    public SDKResponse createPlanTyped(
            String objectiveId,
            String objective,
            PlanningTypes.PlanningScope scope,
            PlanningConstraints constraints
    ) {
        Objects.requireNonNull(objectiveId, "objectiveId must not be null");
        Objects.requireNonNull(objective, "objective must not be null");
        Objects.requireNonNull(scope, "scope must not be null");

        if (runtime != null) {
            PlanningService planningService = runtime.planningService();
            if (planningService != null) {
                PlanningService.PlanningRequest planningRequest =
                        new PlanningService.PlanningRequest(
                                objectiveId,
                                scope,
                                constraints != null
                                        ? constraints
                                        : new PlanningConstraints(
                                                Map.of(),
                                                Map.of(),
                                                Map.of(),
                                                Map.of("objectiveDescription", objective)
                                        )
                        );
                String planId = planningService.createPlan(planningRequest);
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("planId", planId);
                payload.put("objectiveId", objectiveId);
                payload.put("scope", scope.name());
                payload.put("_planningSource", "typed-runtime");
                return SDKResponse.builder()
                        .answer("Plan created: " + planId)
                        .structuredPayload(payload)
                        .build();
            }
        }
        return createPlan(objectiveId, objective, scope.name());
    }

    /**
     * Refines an existing plan based on new information or constraints.
     *
     * <p>When a {@link Runtime} is available, this method delegates directly
     * to {@link PlanningService#refinePlan(PlanningService.PlanRefinementRequest)}.
     * Otherwise it falls back to the legacy string-routing path
     * ({@link #refinePlan(String, String)}).</p>
     *
     * <p><b>Runtime path:</b>
     * {@code PlanningSDK → Runtime.planningService().refinePlan(request)
     *   → DefaultPlanningService}.</p>
     *
     * @param planId the plan identifier (must not be null/blank)
     * @param refinementContextId the refinement context identifier
     * @param refinementDescription the refinement context description
     * @param updatedConstraints the updated constraints (may be null — defaults to empty)
     * @return a structured SDKResponse with the refined plan identifier
     * @throws IllegalArgumentException if planId, refinementContextId, or
     *                                  refinementDescription is null/blank
     */
    public SDKResponse refinePlanTyped(
            String planId,
            String refinementContextId,
            String refinementDescription,
            PlanningConstraints updatedConstraints
    ) {
        Objects.requireNonNull(planId, "planId must not be null");
        Objects.requireNonNull(refinementContextId, "refinementContextId must not be null");
        Objects.requireNonNull(refinementDescription, "refinementDescription must not be null");

        if (runtime != null) {
            PlanningService planningService = runtime.planningService();
            if (planningService != null) {
                PlanningService.PlanRefinementRequest request =
                        new PlanningService.PlanRefinementRequest(
                                planId,
                                new PlanningTypes.RefinementContext(
                                        refinementContextId,
                                        refinementDescription
                                ),
                                updatedConstraints != null
                                        ? updatedConstraints
                                        : new PlanningConstraints(
                                                Map.of(),
                                                Map.of(),
                                                Map.of(),
                                                Map.of()
                                        )
                        );
                String refinedPlanId = planningService.refinePlan(request);
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("planId", refinedPlanId);
                payload.put("refinementContextId", refinementContextId);
                payload.put("_planningSource", "typed-runtime");
                return SDKResponse.builder()
                        .answer("Plan refined: " + refinedPlanId)
                        .structuredPayload(payload)
                        .build();
            }
        }
        return refinePlan(planId, refinementDescription);
    }

    /**
     * Validates a plan against supplied validation criteria.
     *
     * <p>When a {@link Runtime} is available, this method delegates directly
     * to {@link PlanningService#validatePlan(PlanningService.PlanValidationRequest)}
     * using the typed ValidationCriteria model. Otherwise it falls back to
     * the legacy string-routing path ({@link #validatePlan(String)}).</p>
     *
     * <p><b>Runtime path:</b>
     * {@code PlanningSDK → Runtime.planningService().validatePlan(request)
     *   → DefaultPlanningService → PlanningProcessingEngine}.</p>
     *
     * @param planId the plan identifier (must not be null/blank)
     * @param criteria the validation criteria (must not be null)
     * @return a structured SDKResponse with the validation result identifier
     * @throws IllegalArgumentException if planId is null/blank or criteria is null
     */
    public SDKResponse validatePlanTyped(
            String planId,
            ValidationCriteria criteria
    ) {
        Objects.requireNonNull(planId, "planId must not be null");
        Objects.requireNonNull(criteria, "criteria must not be null");

        if (runtime != null) {
            PlanningService planningService = runtime.planningService();
            if (planningService != null) {
                PlanningService.PlanValidationRequest request =
                        new PlanningService.PlanValidationRequest(planId, criteria);
                String validationResultId = planningService.validatePlan(request);
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("planId", planId);
                payload.put("validationResultId", validationResultId);
                payload.put("validationRules", criteria.validationRules());
                payload.put("requiredConditions", criteria.requiredConditions());
                payload.put("completenessRequirements", criteria.completenessRequirements());
                payload.put("_planningSource", "typed-runtime");
                return SDKResponse.builder()
                        .answer("Plan validated: " + validationResultId)
                        .structuredPayload(payload)
                        .build();
            }
        }
        return validatePlan(planId);
    }

    /**
     * Returns the typed {@link PlanningService} from the runtime, or
     * {@code null} when no runtime is available.
     *
     * <p>Exposed for advanced callers (orchestrators, integration tests) that
     * need direct access to the canonical Planning Kernel contract.</p>
     *
     * @return the planning service, or null when no runtime is wired
     */
    public PlanningService planningService() {
        return runtime != null ? runtime.planningService() : null;
    }
}
