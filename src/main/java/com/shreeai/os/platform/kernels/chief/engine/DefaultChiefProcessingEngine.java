package com.shreeai.os.platform.kernels.chief.engine;

import com.shreeai.os.platform.kernels.chief.model.*;
import com.shreeai.os.platform.kernels.multiagent.api.AgentOrchestrator;
import com.shreeai.os.platform.kernels.multiagent.model.AgentResponse;
import com.shreeai.os.platform.kernels.multiagent.model.ParallelExecutionPolicy;
import com.shreeai.os.platform.kernels.multiagent.model.ParallelOrchestrationResult;

import java.time.Instant;
import java.util.*;

/**
 * DefaultChiefProcessingEngine
 *
 * Constitutional Chief Engine responsible for strategic orchestration.
 *
 * Rules:
 * - Chief is the only component allowed to delegate work.
 * - Delegation happens through AgentOrchestrator.
 * - Backward compatibility is preserved via the default constructor.
 */
public final class DefaultChiefProcessingEngine
        implements ChiefProcessingEngine {

    private final AgentOrchestrator orchestrator;

    /**
     * Backward-compatible constructor.
     *
     * Used by DefaultKernelFactory until dependency injection
     * is fully wired. Returns an empty orchestration result.
     */
    public DefaultChiefProcessingEngine() {
        this(new NoOpOrchestrator());
    }

    /**
     * Preferred constructor.
     */
    public DefaultChiefProcessingEngine(
            AgentOrchestrator orchestrator
    ) {
        this.orchestrator = Objects.requireNonNull(
                orchestrator,
                "AgentOrchestrator must not be null"
        );
    }

    @Override
    public ChiefResponse process(ChiefRequest request) {

        Objects.requireNonNull(
                request,
                "ChiefRequest must not be null"
        );

        String objective = String.valueOf(
                request.payload()
                        .getOrDefault("objective", "")
        );

        List<AgentResponse> responses =
                orchestrator.orchestrate(
                        objective,
                        request.metadata()
                );

        boolean success = responses.stream().allMatch(AgentResponse::success);

        return buildResponse(
                request,
                objective,
                responses,
                success,
                Map.of()
        );
    }

    @Override
    public ChiefResponse processWithRetry(
            ChiefRequest request,
            RetryPolicy policy) {

        Objects.requireNonNull(
                request,
                "ChiefRequest must not be null"
        );
        Objects.requireNonNull(
                policy,
                "RetryPolicy must not be null"
        );

        String objective = objectiveOf(request);

        int attempts = 0;
        List<AgentResponse> responses;

        // Primary attempt.
        responses = orchestrator.orchestrate(
                objective,
                request.metadata()
        );
        attempts = 1;

        boolean hasFailure = responses.stream().anyMatch(r -> !r.success());

        while (hasFailure && attempts <= policy.maxRetries()) {
            sleepBackoff(policy, attempts);
            responses = orchestrator.orchestrate(
                    objective,
                    request.metadata()
            );
            attempts++;
            hasFailure = responses.stream().anyMatch(r -> !r.success());
        }

        boolean success = !hasFailure;
        boolean escalated = hasFailure &&
                confidenceOf(request) < policy.escalationThreshold();

        Map<String, Object> retryMetadata = new HashMap<>();
        retryMetadata.put("retryAttempts", attempts - 1);
        retryMetadata.put("maxRetries", policy.maxRetries());
        retryMetadata.put("exhausted", hasFailure);
        retryMetadata.put("escalated", escalated);
        retryMetadata.put("policy", policy);

        return buildResponse(
                request,
                objective,
                responses,
                success,
                retryMetadata
        );
    }

    /**
     * Extracts the objective from the request payload.
     */
    private String objectiveOf(ChiefRequest request) {
        return String.valueOf(
                request.payload()
                        .getOrDefault("objective", "")
        );
    }

    /**
     * Derives a conservative execution confidence from the request payload,
     * defaulting to a neutral 0.5 when absent.
     */
    private double confidenceOf(ChiefRequest request) {
        Object value = request.payload().get("confidence");
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 0.5;
    }

    /**
     * Applies exponential backoff between retry attempts.
     */
    private void sleepBackoff(RetryPolicy policy, int attempt) {
        long backoff = policy.backoffForAttempt(attempt - 1);
        if (backoff <= 0) {
            return;
        }
        try {
            Thread.sleep(backoff);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Builds a canonical ChiefResponse including the orchestration snapshot
     * and any supplied governance metadata.
     */
    private ChiefResponse buildResponse(
            ChiefRequest request,
            String objective,
            List<AgentResponse> responses,
            boolean success,
            Map<String, Object> additionalMetadata) {

        Map<String, Object> orchestrationData = new HashMap<>();
        orchestrationData.put("objective", objective);
        orchestrationData.put("agentCount", responses.size());
        orchestrationData.put("responses", responses);

        ChiefProcessingResult processingResult =
                new ChiefProcessingResult(
                        request.chiefId(),
                        null,
                        null,
                        null,
                        Collections.emptyList(),
                        orchestrationData,
                        Instant.now()
                );

        Map<String, Object> responseMetadata = new HashMap<>();
        responseMetadata.put("result", processingResult);
        responseMetadata.putAll(additionalMetadata);

        return new ChiefResponse(
                request.chiefId(),
                success,
                success
                        ? "Chief successfully orchestrated agents"
                        : "Chief orchestration completed with failures",
                null,
                null,
                null,
                Instant.now(),
                responseMetadata
        );
    }

    /**
     * No-op orchestrator used by the backward-compatible default constructor.
     * Returns no responses for sequential orchestration and an empty result
     * for parallel orchestration.
     */
    private static final class NoOpOrchestrator implements AgentOrchestrator {
        @Override
        public List<AgentResponse> orchestrate(
                String objective, Map<String, Object> context) {
            return List.of();
        }

        @Override
        public ParallelOrchestrationResult parallelOrchestrate(
                String objective,
                Map<String, Object> context,
                ParallelExecutionPolicy policy) {
            Instant now = Instant.now();
            return new ParallelOrchestrationResult(
                    objective, List.of(), 0, 0, 0, now, now, Map.of());
        }
    }
}

