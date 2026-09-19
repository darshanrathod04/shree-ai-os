package com.shreeai.os.platform.runtime.graph;

import com.shreeai.os.platform.graph.ExecutionGraph;
import com.shreeai.os.platform.graph.ExecutionNode;
import com.shreeai.os.platform.resolver.CapabilityType;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.execution.ExecutionResult;
import com.shreeai.os.platform.runtime.execution.ExecutionSession;
import com.shreeai.os.platform.runtime.graph.RuntimeInterceptor.InterceptionDecision;
import com.shreeai.os.platform.runtime.graph.RuntimeInterceptor.Phase;
import com.shreeai.os.platform.runtime.interceptor.DefaultSafetyInterceptor;
import com.shreeai.os.platform.runtime.interceptor.InterceptorResult;
import com.shreeai.os.platform.runtime.interceptor.PermissionContext;
import com.shreeai.os.platform.runtime.interceptor.ValidationContext;
import com.shreeai.os.platform.runtime.interceptor.ValidationInterceptor;
import com.shreeai.os.platform.runtime.interceptor.ValidationResult;
import com.shreeai.os.platform.runtime.interceptor.DefaultValidationInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <b>DefaultGraphRuntimeExecutor</b>
 *
 * <p>Default implementation of {@link GraphRuntimeExecutor}. Executes the
 * Universal Execution Graph produced by the Task-003 builder.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Finds executable nodes (nodes whose {@link NodeExecutor} is registered).</li>
 *   <li>Respects node dependencies via a deterministic topological (Kahn) order.</li>
 *   <li>Runs governance interceptors around each node:
 *       Safety (BEFORE) → node execution → Validation (AFTER), with
 *       Observability (ALWAYS) on every event. Interceptors are NOT nodes.</li>
 *   <li>Produces per-node {@link ExecutionNodeResult}s.</li>
 *   <li>Stops on critical failures: downstream nodes of a failed or blocked
 *       node are SKIPPED and the session is FAILED.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel (Graph Execution)</p>
 */
public final class DefaultGraphRuntimeExecutor implements GraphRuntimeExecutor {

    /** Maximum number of validation retries before a node is marked FAILED. */
    private static final int MAX_RETRIES = 2;

    private final Map<CapabilityType, NodeExecutor> nodeExecutors;
    private final List<RuntimeInterceptor> interceptors;
    private final com.shreeai.os.platform.runtime.interceptor.SafetyInterceptor safetyInterceptor;
    private final ValidationInterceptor validationInterceptor;

    /**
     * Creates an executor with the given node executors and interceptors.
     *
     * @param nodeExecutors capability → executor mapping (never null)
     * @param interceptors  governance interceptors (never null)
     */
    public DefaultGraphRuntimeExecutor(
            Map<CapabilityType, NodeExecutor> nodeExecutors,
            List<RuntimeInterceptor> interceptors) {
        this(nodeExecutors, interceptors, new DefaultSafetyInterceptor(),
                new DefaultValidationInterceptor());
    }

    /**
     * Creates an executor with the given node executors, governance interceptors
     * and a runtime safety gate.
     *
     * @param nodeExecutors     capability → executor mapping (never null)
     * @param interceptors      governance interceptors (never null)
     * @param safetyInterceptor the runtime safety gate (never null)
     */
    public DefaultGraphRuntimeExecutor(
            Map<CapabilityType, NodeExecutor> nodeExecutors,
            List<RuntimeInterceptor> interceptors,
            com.shreeai.os.platform.runtime.interceptor.SafetyInterceptor safetyInterceptor) {
        this(nodeExecutors, interceptors, safetyInterceptor,
                new DefaultValidationInterceptor());
    }

    /**
     * Creates an executor with the given node executors, governance interceptors,
     * safety gate and validation interceptor.
     *
     * @param nodeExecutors       capability → executor mapping (never null)
     * @param interceptors        governance interceptors (never null)
     * @param safetyInterceptor   the runtime safety gate (never null)
     * @param validationInterceptor the post-execution validation interceptor (never null)
     */
    public DefaultGraphRuntimeExecutor(
            Map<CapabilityType, NodeExecutor> nodeExecutors,
            List<RuntimeInterceptor> interceptors,
            com.shreeai.os.platform.runtime.interceptor.SafetyInterceptor safetyInterceptor,
            ValidationInterceptor validationInterceptor) {
        this.nodeExecutors = Objects.requireNonNull(nodeExecutors, "nodeExecutors must not be null");
        this.interceptors = List.copyOf(Objects.requireNonNull(interceptors, "interceptors must not be null"));
        this.safetyInterceptor = Objects.requireNonNull(safetyInterceptor, "safetyInterceptor must not be null");
        this.validationInterceptor = Objects.requireNonNull(validationInterceptor, "validationInterceptor must not be null");
    }

    /**
     * Creates an executor with the given node executors and the standard
     * governance interceptor chain (Safety → Validation, Observability always).
     *
     * @param nodeExecutors capability → executor mapping (never null)
     */
    public DefaultGraphRuntimeExecutor(Map<CapabilityType, NodeExecutor> nodeExecutors) {
        this(nodeExecutors, List.of(
                new ObservabilityInterceptor(),
                new SafetyInterceptor(),
                new com.shreeai.os.platform.runtime.graph.ValidationInterceptor()));
    }

    @Override
    public ExecutionSession execute(ExecutionGraph graph, ExecutionRequest request) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(request, "request must not be null");

        long startNanos = System.nanoTime();

        // 1. Find executable nodes (those with a registered NodeExecutor).
        Map<CapabilityType, ExecutionNode> nodeByCapability = new EnumMap<>(CapabilityType.class);
        for (ExecutionNode node : graph.nodes()) {
            nodeByCapability.put(node.capability(), node);
        }

        List<ExecutionNode> executable = new ArrayList<>();
        for (ExecutionNode node : graph.nodes()) {
            if (nodeExecutors.containsKey(node.capability())) {
                executable.add(node);
            }
        }
        executable.sort(Comparator.comparing(ExecutionNode::nodeId));

        // 2. Deterministic topological order over the executable sub-graph.
        List<ExecutionNode> ordered = topologicalOrder(executable, nodeByCapability);

        // 3. Execute in order with interceptors.
        Map<String, Object> executionContext = new LinkedHashMap<>();
        Map<CapabilityType, ExecutionNodeResult> resultsByCapability =
                new EnumMap<>(CapabilityType.class);
        List<ExecutionNodeResult> nodeResults = new ArrayList<>();
        boolean stopped = false;
        List<InterceptorResult> interceptionResults = new ArrayList<>();

        for (ExecutionNode node : ordered) {
            if (stopped) {
                ExecutionNodeResult skipped = ExecutionNodeResult.skipped(
                        node.nodeId(), node.capability(),
                        "skipped: graph execution stopped after critical failure");
                nodeResults.add(skipped);
                resultsByCapability.put(node.capability(), skipped);
                continue;
            }

            // Dependencies must have completed.
            String unmet = unmetDependency(node, resultsByCapability);
            if (unmet != null) {
                ExecutionNodeResult skipped = ExecutionNodeResult.skipped(
                        node.nodeId(), node.capability(), unmet);
                nodeResults.add(skipped);
                resultsByCapability.put(node.capability(), skipped);
                continue;
            }

            // Runtime safety gate (canonical pre-node security check).
            PermissionContext permissionContext = PermissionContext.of(node, request);
            InterceptorResult safety = safetyInterceptor.before(node, permissionContext);
            if (!safety.isAllowed()) {
                ExecutionNodeResult blocked = ExecutionNodeResult.blocked(
                        node.nodeId(), node.capability(), safety.reason());
                nodeResults.add(blocked);
                resultsByCapability.put(node.capability(), blocked);
                safety = safety.withNodeId(node.nodeId());
                interceptionResults.add(safety);
                stopped = true;
                continue;
            }

            // BEFORE interceptors (Safety) — may block the node.
            InterceptionDecision decision = InterceptionDecision.proceed();
            for (RuntimeInterceptor interceptor : interceptors) {
                if (interceptor.phase() == Phase.BEFORE
                        || interceptor.phase() == Phase.ALWAYS) {
                    decision = interceptor.beforeNode(node, request);
                    if (!decision.isProceed()) {
                        break;
                    }
                }
            }
            if (!decision.isProceed()) {
                ExecutionNodeResult blocked = ExecutionNodeResult.blocked(
                        node.nodeId(), node.capability(), decision.blockReason());
                nodeResults.add(blocked);
                resultsByCapability.put(node.capability(), blocked);
                stopped = true;
                continue;
            }

            // Execute the node with validation retry.
            int attempt = 1;
            ExecutionNodeResult result = null;
            boolean nodeFailed = false;

            while (attempt <= MAX_RETRIES) {
                // Execute the node.
                long nodeStart = System.nanoTime();
                try {
                    result = nodeExecutors.get(node.capability())
                            .execute(node, request, executionContext);
                    if (result == null) {
                        result = ExecutionNodeResult.failed(
                                node.nodeId(), node.capability(),
                                "node executor returned null result", 0L);
                    }
                } catch (RuntimeException error) {
                    for (RuntimeInterceptor interceptor : interceptors) {
                        interceptor.onNodeError(node, error);
                    }
                    result = ExecutionNodeResult.failed(
                            node.nodeId(), node.capability(),
                            "node execution error: " + error.getMessage(), 0L);
                }
                long durationMillis = (System.nanoTime() - nodeStart) / 1_000_000L;
                if (result.durationMillis() <= 0 && result.isSuccess()) {
                    result = ExecutionNodeResult.completed(
                            node.nodeId(), node.capability(),
                            result.output(), result.confidence(), durationMillis);
                }

                // Post-execution validation.
                ValidationContext valCtx = ValidationContext.of(
                        node, result, request, executionContext, attempt);
                ValidationResult valResult = validationInterceptor.validate(valCtx);

                if (valResult.isPass()) {
                    break; // Output is acceptable — continue to next node.
                }
                if (valResult.isRetry() && attempt < MAX_RETRIES) {
                    attempt++;
                    continue; // Retry the node.
                }
                // FAIL or final retry exceeded — convert to FAILED.
                result = ExecutionNodeResult.failed(
                        node.nodeId(), node.capability(),
                        "Validation: " + String.join("; ", valResult.issues()),
                        result.durationMillis());
                nodeFailed = true;
                break;
            }

            // AFTER interceptors (Validation) may replace the result.
            for (RuntimeInterceptor interceptor : interceptors) {
                if (interceptor.phase() == Phase.AFTER
                        || interceptor.phase() == Phase.ALWAYS) {
                    result = interceptor.afterNode(node, result);
                }
            }

            nodeResults.add(result);
            resultsByCapability.put(node.capability(), result);
            if (nodeFailed || !result.isSuccess()) {
                // Stop on critical failures.
                stopped = true;
            } else {
                executionContext.put("node:" + node.capability().name(), result.output());
            }
        }

        long totalMillis = (System.nanoTime() - startNanos) / 1_000_000L;
        return buildSession(graph, request, nodeResults, interceptionResults, totalMillis);
    }

    /**
     * Returns a reason string when a dependency has not completed, or null
     * when every dependency completed successfully.
     */
    private String unmetDependency(
            ExecutionNode node,
            Map<CapabilityType, ExecutionNodeResult> resultsByCapability) {
        for (String depNodeId : node.dependencies()) {
            CapabilityType depCap = CapabilityType.valueOf(depNodeId);
            ExecutionNodeResult dependencyResult = resultsByCapability.get(depCap);
            if (dependencyResult == null) {
                return "skipped: dependency " + depCap + " was not executed";
            }
            if (!dependencyResult.isSuccess()) {
                return "skipped: dependency " + depCap + " did not complete ("
                        + dependencyResult.status() + ")";
            }
        }
        return null;
    }

    /**
     * Kahn topological sort preserving deterministic (nodeId) tie-breaking.
     */
    private List<ExecutionNode> topologicalOrder(
            List<ExecutionNode> nodes,
            Map<CapabilityType, ExecutionNode> nodeByCapability) {
        Map<CapabilityType, Integer> inDegree = new EnumMap<>(CapabilityType.class);
        Map<CapabilityType, Set<CapabilityType>> dependents = new EnumMap<>(CapabilityType.class);

        for (ExecutionNode node : nodes) {
            inDegree.putIfAbsent(node.capability(), 0);
        }
        for (ExecutionNode node : nodes) {
            for (String depNodeId : node.dependencies()) {
                CapabilityType dependency = CapabilityType.valueOf(depNodeId);
                if (nodeByCapability.containsKey(dependency)
                        && !dependency.equals(node.capability())) {
                    inDegree.merge(node.capability(), 1, Integer::sum);
                    dependents.computeIfAbsent(dependency, k -> new LinkedHashSet<>())
                            .add(node.capability());
                }
            }
        }

        List<ExecutionNode> ready = new ArrayList<>();
        for (ExecutionNode node : nodes) {
            if (inDegree.get(node.capability()) == 0) {
                ready.add(node);
            }
        }
        ready.sort(Comparator.comparing(ExecutionNode::nodeId));

        List<ExecutionNode> ordered = new ArrayList<>();
        while (!ready.isEmpty()) {
            ExecutionNode current = ready.remove(0);
            ordered.add(current);
            for (CapabilityType dependent : dependents.getOrDefault(
                    current.capability(), Collections.emptySet())) {
                inDegree.merge(dependent, -1, Integer::sum);
                if (inDegree.get(dependent) == 0) {
                    ready.add(nodeByCapability.get(dependent));
                }
            }
            ready.sort(Comparator.comparing(ExecutionNode::nodeId));
        }
        return ordered;
    }

    /**
     * Aggregates node results into the terminal {@link ExecutionSession}.
     */
    private ExecutionSession buildSession(
            ExecutionGraph graph,
            ExecutionRequest request,
            List<ExecutionNodeResult> nodeResults,
            List<InterceptorResult> interceptionResults,
            long totalMillis) {

        long completed = nodeResults.stream().filter(ExecutionNodeResult::isSuccess).count();
        long failed = nodeResults.stream()
                .filter(r -> r.status() == ExecutionNodeResult.NodeStatus.FAILED).count();
        long blocked = nodeResults.stream()
                .filter(r -> r.status() == ExecutionNodeResult.NodeStatus.BLOCKED).count();

        // Aggregate output: completed node outputs joined in execution order.
        StringBuilder output = new StringBuilder();
        double confidenceSum = 0.0;
        for (ExecutionNodeResult result : nodeResults) {
            if (result.isSuccess() && !result.output().isBlank()) {
                if (output.length() > 0) {
                    output.append("\n");
                }
                output.append(result.output());
                confidenceSum += result.confidence();
            }
        }

        boolean success = failed == 0 && blocked == 0 && completed > 0;
        String requestId = request.requestId() != null ? request.requestId() : graph.requestId();

        // Structured runtime payload describing governance events: safety blocks
        // and approval requests. It is omitted when nothing was intercepted, so
        // legacy callers still observe the flat output / errorMessage behaviour.
        Map<String, Object> structuredPayload = new LinkedHashMap<>();
        List<Map<String, Object>> safetyInterceptions = new ArrayList<>();
        List<String> approvals = new ArrayList<>();
        for (InterceptorResult ir : interceptionResults) {
            if (ir.isAllowed()) {
                continue;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("nodeId", ir.nodeId());
            entry.put("outcome", ir.outcome().name());
            entry.put("reason", ir.reason());
            if (ir.approvalRequestId() != null && !ir.approvalRequestId().isBlank()) {
                entry.put("approvalRequestId", ir.approvalRequestId());
                approvals.add(ir.approvalRequestId());
            }
            safetyInterceptions.add(entry);
        }
        if (!safetyInterceptions.isEmpty()) {
            structuredPayload.put("safetyInterceptions", safetyInterceptions);
        }
        if (!approvals.isEmpty()) {
            structuredPayload.put("executionState", "REQUIRES_APPROVAL");
            structuredPayload.put("executionApprovals", approvals);
        }

        ExecutionResult.Builder resultBuilder = success
                ? ExecutionResult.builder()
                .requestId(requestId)
                .success(true)
                .output(output.toString())
                : ExecutionResult.builder()
                .requestId(requestId)
                .success(false)
                .errorMessage(firstFailureMessage(nodeResults));
        if (!structuredPayload.isEmpty()) {
            resultBuilder.structuredPayload(structuredPayload);
        }
        ExecutionResult result = resultBuilder.build();

        return ExecutionSession.builder()
                .sessionId(graph.graphId())
                .requestId(requestId)
                .status(success
                        ? ExecutionSession.SessionStatus.COMPLETED
                        : ExecutionSession.SessionStatus.FAILED)
                .result(result)
                .build();
    }

    private String firstFailureMessage(List<ExecutionNodeResult> nodeResults) {
        for (ExecutionNodeResult result : nodeResults) {
            if (!result.isSuccess() && !result.message().isBlank()) {
                return result.message();
            }
        }
        return "Graph execution failed";
    }
}
