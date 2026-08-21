package com.shreeai.os.platform.sdk;

import java.util.Map;
import java.util.Objects;

/**
 * Planning SDK Facade
 *
 * Developer-facing wrapper over the Planning Kernel.
 * Contains no planning logic; delegates through ShreeClient.
 */
public final class PlanningSDK {

    private final ShreeClient client;

    PlanningSDK(ShreeClient client) {
        this.client = Objects.requireNonNull(client);
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
}