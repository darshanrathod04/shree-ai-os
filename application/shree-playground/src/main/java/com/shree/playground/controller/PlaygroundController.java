package com.shree.playground.controller;

import com.shree.playground.dto.*;
import com.shree.playground.service.MemoryService;
import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/playground")
public class PlaygroundController {

    private final ShreeAI ai;
    private final MemoryService memoryService;

     public PlaygroundController(ShreeAI ai, MemoryService memoryService) {
        this.ai = ai;
        this.memoryService = memoryService;
    }

    // =====================================================
    // CHAT
    // =====================================================

    @PostMapping("/chat")
    public SDKResponse chat(@RequestBody ChatRequest request) {
        return ai.chat(request.message());
    }

    // =====================================================
    // IDENTITY KERNEL
    // =====================================================

    @PostMapping("/identity/create")
    public SDKResponse createIdentity(
            @RequestBody IdentityCreateRequest request
    ) {
        return ai.identity().createIdentity(
                request.identityId(),
                request.identityType(),
                request.profile()
        );
    }

    @GetMapping("/identity/{id}")
    public SDKResponse getIdentity(@PathVariable String id) {
        return ai.identity().getIdentity(id);
    }

    @PostMapping("/identity/update")
    public SDKResponse updateIdentity(
            @RequestBody IdentityUpdateRequest request
    ) {
        return ai.identity().updateProfile(
                request.identityId(),
                request.updates()
        );
    }

    // =====================================================
    // MEMORY KERNEL
    // =====================================================

    @PostMapping("/memory/store")
    public SDKResponse storeMemory(
            @RequestBody MemoryStoreRequest request
    ) {
        return memoryService.store(
                request.title(),
                request.content()
        );
    }

    @PostMapping("/memory/search")
    public SDKResponse searchMemory(
            @RequestBody SearchRequest request
    ) {
        return memoryService.search(request.query());
    }

    @PostMapping("/memory/recall")
    public SDKResponse recallMemory(
            @RequestBody SearchRequest request
    ) {
        return memoryService.recall(request.query());
    }

    // =====================================================
    // KNOWLEDGE KERNEL
    // =====================================================

    @PostMapping("/knowledge/search")
    public SDKResponse searchKnowledge(
            @RequestBody SearchRequest request
    ) {
        return ai.knowledge().search(request.query());
    }

    @PostMapping("/knowledge/query")
    public SDKResponse queryKnowledge(
            @RequestBody SearchRequest request
    ) {
        return ai.knowledge().query(request.query());
    }

    @PostMapping("/knowledge/retrieve")
    public SDKResponse retrieveKnowledge(
            @RequestBody RetrieveRequest request
    ) {
        return ai.knowledge().retrieve(request.entityId());
    }

    @PostMapping("/knowledge/ingest")
    public SDKResponse ingestKnowledge(
            @RequestBody KnowledgeIngestRequest request
    ) {
        return ai.knowledge().ingest(
                request.title(),
                request.content()
        );
    }

    // =====================================================
    // PLANNING KERNEL
    // =====================================================

    @PostMapping("/planning/create")
    public SDKResponse createPlan(
            @RequestBody PlanCreateRequest request
    ) {
        return ai.planning().createPlan(
                request.objectiveId(),
                request.objective(),
                request.scope()
        );
    }

    @PostMapping("/planning/refine")
    public SDKResponse refinePlan(
            @RequestBody PlanRefineRequest request
    ) {
        return ai.planning().refinePlan(
                request.planId(),
                request.refinement()
        );
    }

    @PostMapping("/planning/validate")
    public SDKResponse validatePlan(
            @RequestBody PlanValidateRequest request
    ) {
        return ai.planning().validatePlan(request.planId());
    }

    // =====================================================
    // EXECUTION KERNEL
    // =====================================================

    @PostMapping("/execution")
    public SDKResponse execute(
            @RequestBody ExecutionRequest request
    ) {
        return ai.execution().execute(
                request.capability(),
                request.input()
        );
    }

    // =====================================================
    // REFLECTION KERNEL
    // =====================================================

    @PostMapping("/reflection/run")
    public SDKResponse reflect(
            @RequestBody ReflectionRequest request
    ) {
        return ai.reflection().reflect(request.executionId());
    }

    @GetMapping("/reflection/history")
    public SDKResponse reflectionHistory(
            @RequestParam(defaultValue = "default") String tenant,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ai.reflection().getHistory(tenant, limit);
    }

    @GetMapping("/reflection/analytics")
    public SDKResponse reflectionAnalytics(
            @RequestParam(defaultValue = "default") String tenant,
            @RequestParam(defaultValue = "50") int window
    ) {
        return ai.reflection().getAnalytics(tenant, window);
    }
}