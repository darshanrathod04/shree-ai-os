package com.shreeai.os.platform.runtime.graph;

import com.shreeai.os.platform.graph.ExecutionNode;
import com.shreeai.os.platform.kernels.identity.api.IdentityService;
import com.shreeai.os.platform.kernels.identity.model.IdentityContext;
import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeSearchService;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.memory.api.MemorySearchService;
import com.shreeai.os.platform.kernels.memory.model.Memory;
import com.shreeai.os.platform.kernels.memory.model.MemoryContent;
import com.shreeai.os.platform.kernels.planning.api.PlanningService;
import com.shreeai.os.platform.kernels.planning.api.PlanningTypes;
import com.shreeai.os.platform.kernels.planning.model.PlanningConstraints;
import com.shreeai.os.platform.resolver.CapabilityType;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>CapabilityNodeExecutors</b>
 *
 * <p>Factory that builds the {@code Map<CapabilityType, NodeExecutor>} consumed by
 * {@link DefaultGraphRuntimeExecutor}. Each lambda delegates to the existing
 * kernel service that owns the capability, bridging the Universal Execution Graph
 * (Task-003) to the canonical kernel layer.</p>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Never throws: failures are reported as {@link ExecutionNodeResult#failed() failed}
 *       node results so the executor can decide whether to stop the graph.</li>
 *   <li>Each executor reads its input from {@link ExecutionRequest#payload() payload}
 *       (or metadata for IDENTITY) and produces a textual representation of the
 *       kernel's domain output.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel (Graph Execution)</p>
 *
 * @see DefaultGraphRuntimeExecutor
 * @see NodeExecutor
 */
public final class CapabilityNodeExecutors {

    private CapabilityNodeExecutors() {
        // Utility class — no instances.
    }

    /**
     * Builds the canonical capability-to-executor mapping from the kernel services
     * wired into the {@link com.shreeai.os.platform.runtime.service.DefaultRuntimeService}.
     *
     * <p>The mapping is built incrementally: only services that are non-null are
     * registered. This allows partial graphs (e.g. a graph that only requires
     * MEMORY and KNOWLEDGE) to execute even when unrelated kernel services have
     * not been initialised.
     *
     * @param identityService       the identity kernel service (may be null)
     * @param knowledgeSearchService the knowledge search service (may be null)
     * @param planningService       the planning kernel service (may be null)
     * @param memorySearchService   the memory search service (may be null)
     * @return an unmodifiable mapping of capability to node executor (never null, may be empty)
     */
    public static Map<CapabilityType, NodeExecutor> buildNodeExecutors(
            IdentityService identityService,
            KnowledgeSearchService knowledgeSearchService,
            PlanningService planningService,
            MemorySearchService memorySearchService) {

        Map<CapabilityType, NodeExecutor> executors = new LinkedHashMap<>();

        if (identityService != null) {
            executors.put(CapabilityType.IDENTITY, newIdentityExecutor(identityService));
        }
        if (knowledgeSearchService != null) {
            executors.put(CapabilityType.KNOWLEDGE, newKnowledgeExecutor(knowledgeSearchService));
        }
        if (planningService != null) {
            executors.put(CapabilityType.PLANNING, newPlanningExecutor(planningService));
        }
        if (memorySearchService != null) {
            executors.put(CapabilityType.MEMORY, newMemoryExecutor(memorySearchService));
        }

        return Map.copyOf(executors);
    }

    // ─── Per-capability executors ──────────────────────────────────────────

    /**
     * IDENTITY executor: resolves the agent/application identity from the request
     * metadata (requestId, sessionId, applicationId, workspaceId) and returns
     * the resolved identity ID as the node output.
     */
    private static NodeExecutor newIdentityExecutor(IdentityService identityService) {
        return (node, request, context) -> {
            long start = System.nanoTime();
            try {
                Map<String, Object> meta = request.metadata() != null
                        ? request.metadata() : Map.of();
                String requestId = String.valueOf(meta.getOrDefault("requestId", request.requestId()));
                String sessionId = String.valueOf(meta.getOrDefault("sessionId", ""));
                String applicationId = String.valueOf(meta.getOrDefault("applicationId", ""));
                String workspaceId = String.valueOf(meta.getOrDefault("workspaceId", ""));

                IdentityContext identity = identityService.resolveIdentity(
                        requestId, sessionId, applicationId, workspaceId);

                String output = identity != null && identity.identityId() != null
                        ? identity.identityId().value()
                        : "identity:resolved";

                return ExecutionNodeResult.completed(
                        node.nodeId(), CapabilityType.IDENTITY, output, 0.95,
                        (System.nanoTime() - start) / 1_000_000L);
            } catch (RuntimeException error) {
                return ExecutionNodeResult.failed(
                        node.nodeId(), CapabilityType.IDENTITY,
                        "identity resolution error: " + error.getMessage(),
                        (System.nanoTime() - start) / 1_000_000L);
            }
        };
    }

    /**
     * KNOWLEDGE executor: searches the knowledge base for the request payload
     * and returns the concatenated labels of the matching knowledge nodes.
     */
    private static NodeExecutor newKnowledgeExecutor(KnowledgeSearchService knowledgeSearchService) {
        return (node, request, context) -> {
            long start = System.nanoTime();
            try {
                String query = request.payload() != null ? request.payload() : "";
                List<KnowledgeNode> results = knowledgeSearchService.search(query);

                String output = results.stream()
                        .map(KnowledgeNode::getLabel)
                        .filter(Objects::nonNull)
                        .reduce("", (a, b) -> a.isEmpty() ? b : a + "; " + b);

                if (output.isBlank()) {
                    output = "knowledge: no results for '" + query + "'";
                }

                double confidence = results.isEmpty() ? 0.1 : Math.min(0.95, 0.5 + results.size() * 0.1);
                return ExecutionNodeResult.completed(
                        node.nodeId(), CapabilityType.KNOWLEDGE, output, confidence,
                        (System.nanoTime() - start) / 1_000_000L);
            } catch (RuntimeException error) {
                return ExecutionNodeResult.failed(
                        node.nodeId(), CapabilityType.KNOWLEDGE,
                        "knowledge search error: " + error.getMessage(),
                        (System.nanoTime() - start) / 1_000_000L);
            }
        };
    }

    /**
     * PLANNING executor: creates a plan from the request payload (the user's
     * objective) and returns the resulting plan ID.
     */
    private static NodeExecutor newPlanningExecutor(PlanningService planningService) {
        return (node, request, context) -> {
            long start = System.nanoTime();
            try {
                String objective = request.payload() != null ? request.payload() : "";
                String objectiveId = request.requestId() != null ? request.requestId() : "";

                PlanningService.PlanningRequest planningRequest = new PlanningService.PlanningRequest(
                        objectiveId,
                        PlanningTypes.PlanningScope.STANDARD,
                        emptyPlanningConstraints());

                String planId = planningService.createPlan(planningRequest);

                return ExecutionNodeResult.completed(
                        node.nodeId(), CapabilityType.PLANNING,
                        planId != null ? planId : "plan:created",
                        0.90,
                        (System.nanoTime() - start) / 1_000_000L);
            } catch (RuntimeException error) {
                return ExecutionNodeResult.failed(
                        node.nodeId(), CapabilityType.PLANNING,
                        "planning error: " + error.getMessage(),
                        (System.nanoTime() - start) / 1_000_000L);
            }
        };
    }

    /**
     * MEMORY executor: searches the memory store for the request payload (the
     * search query) and returns the concatenated text of matching memories.
     */
    private static NodeExecutor newMemoryExecutor(MemorySearchService memorySearchService) {
        return (node, request, context) -> {
            long start = System.nanoTime();
            try {
                String query = request.payload() != null ? request.payload() : "";
                List<Memory> results = memorySearchService.search(query);

                String output = results.stream()
                        .map(Memory::content)
                        .map(MemoryContent::text)
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isBlank())
                        .reduce("", (a, b) -> a.isEmpty() ? b : a + "; " + b);

                if (output.isBlank()) {
                    output = "memory: no results for '" + query + "'";
                }

                double confidence = results.isEmpty() ? 0.1 : Math.min(0.95, 0.5 + results.size() * 0.1);
                return ExecutionNodeResult.completed(
                        node.nodeId(), CapabilityType.MEMORY, output, confidence,
                        (System.nanoTime() - start) / 1_000_000L);
            } catch (RuntimeException error) {
                return ExecutionNodeResult.failed(
                        node.nodeId(), CapabilityType.MEMORY,
                        "memory search error: " + error.getMessage(),
                        (System.nanoTime() - start) / 1_000_000L);
            }
        };
    }

    /**
     * Returns empty planning constraints for graph-driven planning nodes.
     */
    private static PlanningConstraints emptyPlanningConstraints() {
        return new PlanningConstraints(Map.of(), Map.of(), Map.of(), Map.of());
    }
}
