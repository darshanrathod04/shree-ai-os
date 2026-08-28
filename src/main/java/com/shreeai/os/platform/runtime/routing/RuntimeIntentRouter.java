package com.shreeai.os.platform.runtime.routing;

import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * <b>RuntimeIntentRouter</b>
 *
 * <p>Deterministic dispatch table that routes SDK requests to the kernel
 * that owns the requested operation, instead of always running the generic
 * Chief reasoning path.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Reads {@code metadata.operation} from the canonical
 *       {@link ExecutionRequest}.</li>
 *   <li>Maps known operations to the owning kernel's canonical stages.</li>
 *   <li>Returns an empty route for unknown/absent operations so the
 *       canonical Chief orchestration pipeline executes unchanged.</li>
 * </ul>
 *
 * <p><b>Routing Table (deterministic, no heuristics):</b></p>
 * <ul>
 *   <li>SEARCH_KNOWLEDGE → Knowledge Kernel</li>
 *   <li>QUERY_KNOWLEDGE → Knowledge Kernel</li>
 *   <li>RETRIEVE_ENTITY → Knowledge Kernel</li>
 *   <li>PLAN_PROJECT → Planning Kernel</li>
 *   <li>CREATE_PLAN → Planning Kernel</li>
 *   <li>RECALL_MEMORY → Memory Kernel</li>
 *   <li>STORE_MEMORY → Memory Kernel</li>
 *   <li>otherwise → Chief orchestration (full canonical pipeline)</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Reuses existing kernel pipeline stages only — no new architecture.</li>
 *   <li>Stateless and thread-safe: routing depends only on the request.</li>
 *   <li>Never mutates requests or results.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Service</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class RuntimeIntentRouter {

    /**
     * Kernel that owns a routed operation.
     */
    public enum TargetKernel {
        KNOWLEDGE,
        PLANNING,
        MEMORY
    }

    /**
     * Immutable routing decision.
     *
     * @param operation  the normalized operation that triggered the route
     * @param kernel     the kernel that owns the operation
     * @param kernelName the human readable kernel name
     * @param stages     the ordered kernel stage chain (immutable)
     */
    public record ExecutionRoute(
            String operation,
            TargetKernel kernel,
            String kernelName,
            List<ExecutionStage> stages) {

        public ExecutionRoute {
            stages = List.copyOf(stages);
        }

        /**
         * Returns the names of the routed stages in execution order.
         *
         * @return immutable list of stage names
         */
        public List<String> stageNames() {
            return stages.stream()
                    .map(stage -> stage.getDescriptor().getStageName())
                    .toList();
        }
    }

    /**
     * Reserved request metadata key carrying the requested operation.
     */
    public static final String OPERATION_METADATA_KEY = "operation";

    /**
     * Deterministic operation → kernel dispatch table.
     */
    private static final Map<String, TargetKernel> OPERATIONS = Map.of(
            "SEARCH_KNOWLEDGE", TargetKernel.KNOWLEDGE,
            "QUERY_KNOWLEDGE", TargetKernel.KNOWLEDGE,
            "RETRIEVE_ENTITY", TargetKernel.KNOWLEDGE,
            "PLAN_PROJECT", TargetKernel.PLANNING,
            "CREATE_PLAN", TargetKernel.PLANNING,
            "RECALL_MEMORY", TargetKernel.MEMORY,
            "STORE_MEMORY", TargetKernel.MEMORY
    );

    private final List<ExecutionStage> knowledgeStages;
    private final List<ExecutionStage> planningStages;
    private final List<ExecutionStage> memoryRecallStages;
    private final List<ExecutionStage> memoryStoreStages;

    /**
     * Creates a router over the canonical kernel pipeline stages.
     *
     * <p>The router never creates stages; it only composes the existing
     * stage instances owned by the Runtime.</p>
     *
     * @param identityStage     the shared identity stage (must not be null)
     * @param contextStage      the shared context stage (must not be null)
     * @param knowledgeStage    the Knowledge Kernel stage (must not be null)
     * @param planningStage     the Planning Kernel stage (must not be null)
     * @param memoryRecallStage the Memory Kernel recall stage (must not be null)
     * @param memoryStoreStage  the Memory Kernel store stage (must not be null)
     */
    public RuntimeIntentRouter(
            ExecutionStage identityStage,
            ExecutionStage contextStage,
            ExecutionStage knowledgeStage,
            ExecutionStage planningStage,
            ExecutionStage memoryRecallStage,
            ExecutionStage memoryStoreStage) {

        Objects.requireNonNull(identityStage, "identityStage must not be null");
        Objects.requireNonNull(contextStage, "contextStage must not be null");
        Objects.requireNonNull(knowledgeStage, "knowledgeStage must not be null");
        Objects.requireNonNull(planningStage, "planningStage must not be null");
        Objects.requireNonNull(memoryRecallStage, "memoryRecallStage must not be null");
        Objects.requireNonNull(memoryStoreStage, "memoryStoreStage must not be null");

        this.knowledgeStages = List.of(identityStage, contextStage, knowledgeStage);
        this.planningStages = List.of(identityStage, contextStage, planningStage);
        this.memoryRecallStages = List.of(identityStage, contextStage, memoryRecallStage);
        this.memoryStoreStages = List.of(identityStage, contextStage, memoryStoreStage);
    }

    /**
     * Resolves the normalized operation from the request metadata.
     *
     * @param request the execution request (may be null)
     * @return the normalized operation, or empty when absent/unknown
     */
    public Optional<String> resolveOperation(ExecutionRequest request) {

        if (request == null || request.metadata() == null) {
            return Optional.empty();
        }

        Object value = request.metadata().get(OPERATION_METADATA_KEY);

        if (value == null) {
            return Optional.empty();
        }

        String operation = String.valueOf(value).trim().toUpperCase(Locale.ROOT);

        if (operation.isEmpty() || !OPERATIONS.containsKey(operation)) {
            return Optional.empty();
        }

        return Optional.of(operation);
    }

    /**
     * Checks whether the request carries a routable kernel operation.
     *
     * @param request the execution request (may be null)
     * @return true when the request routes to a kernel
     */
    public boolean isRouted(ExecutionRequest request) {
        return resolveOperation(request).isPresent();
    }

    /**
     * Resolves the kernel route for the request.
     *
     * @param request the execution request (may be null)
     * @return the execution route, or empty when the request must go through
     *         the existing Chief orchestration path
     */
    public Optional<ExecutionRoute> route(ExecutionRequest request) {

        return resolveOperation(request).map(operation -> {

            TargetKernel kernel = OPERATIONS.get(operation);

            return switch (kernel) {
                case KNOWLEDGE -> new ExecutionRoute(
                        operation,
                        kernel,
                        "Knowledge Kernel",
                        knowledgeStages);
                case PLANNING -> new ExecutionRoute(
                        operation,
                        kernel,
                        "Planning Kernel",
                        planningStages);
                case MEMORY -> "STORE_MEMORY".equals(operation)
                        ? new ExecutionRoute(
                                operation,
                                kernel,
                                "Memory Kernel",
                                memoryStoreStages)
                        : new ExecutionRoute(
                                operation,
                                kernel,
                                "Memory Kernel",
                                memoryRecallStages);
            };
        });
    }
}
