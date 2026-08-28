package com.shreeai.os.platform.kernels.chief.engine;

import com.shreeai.os.platform.kernels.chief.model.*;
import com.shreeai.os.platform.kernels.multiagent.api.AgentOrchestrator;
import com.shreeai.os.platform.kernels.multiagent.model.AgentResponse;

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
        this((objective, context) -> List.of());
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

        return new ChiefResponse(
                request.chiefId(),
                true,
                "Chief successfully orchestrated agents",
                null,
                null,
                null,
                Instant.now(),
                Map.of("result", processingResult)
        );
    }
}