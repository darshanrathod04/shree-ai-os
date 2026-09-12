package com.shreeai.os.platform.graph;

import com.shreeai.os.platform.resolver.CapabilityPlan;
import com.shreeai.os.platform.resolver.CapabilityRequirement;
import com.shreeai.os.platform.resolver.CapabilityType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * <b>ExecutionGraphBuilder</b>
 *
 * <p>Converts a {@link CapabilityPlan} into an {@link ExecutionGraph} by
 * generating nodes and their dependencies automatically.</p>
 *
 * <p><b>Graph rules applied by the builder:</b></p>
 * <ul>
 *   <li>Identity is always the root node.</li>
 *   <li>Context depends on Identity.</li>
 *   <li>Knowledge depends on Context.</li>
 *   <li>Memory depends on Context.</li>
 *   <li>Reasoning depends on Knowledge and/or Memory.</li>
 *   <li>Planning depends on Reasoning.</li>
 *   <li>Execution depends on Planning.</li>
 *   <li>Governance (Safety, Validation, Observability) is applied by
 *       the runtime executor as interceptors, not as graph nodes.</li>
 *   <li>Tools depend on Context.</li>
 *   <li>Modules depend on Reasoning.</li>
 *   <li>Agents depend on Planning.</li>
 *   <li>Observability interceptors observe every node event.</li>
 * </ul>
 *
 * <p>The builder is pure and deterministic: it never executes capabilities,
 * never calls the runtime, and never runs kernels. The produced graph is a
 * planning artifact only.</p>
 *
 * <p><b>Ownership:</b> Platform Graph</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class ExecutionGraphBuilder {

    /**
     * Builds an execution graph for the given capability plan.
     *
     * @param plan the capability plan (must not be null)
     * @return a new execution graph
     */
    public ExecutionGraph build(CapabilityPlan plan) {
        Objects.requireNonNull(plan, "plan must not be null");

        // Ordered set of required capabilities with Identity always first.
        Map<CapabilityType, CapabilityRequirement.Priority> required =
                collectRequired(plan);

        // Automatically generate dependencies from graph rules.
        Map<CapabilityType, List<CapabilityType>> dependencyMap =
                computeDependencies(required.keySet());

        // Build nodes with generated dependencies.
        List<ExecutionNode> nodes = new ArrayList<>();
        Map<CapabilityType, ExecutionNode> nodeByCapability = new LinkedHashMap<>();
        for (Map.Entry<CapabilityType, CapabilityRequirement.Priority> entry
                : required.entrySet()) {
            ExecutionNode node = buildNode(
                    entry.getKey(),
                    entry.getValue(),
                    dependencyMap.getOrDefault(entry.getKey(), List.of()));
            nodes.add(node);
            nodeByCapability.put(entry.getKey(), node);
        }

        // Generate edges from the dependency map.
        List<ExecutionEdge> edges = buildEdges(dependencyMap);

        ExecutionNode root = nodeByCapability.get(CapabilityType.IDENTITY);
        Objects.requireNonNull(root, "Identity node must exist as graph root");

        return ExecutionGraph.builder()
                .graphId("graph-" + UUID.randomUUID())
                .requestId(plan.requestId())
                .nodes(nodes)
                .edges(edges)
                .rootNode(root)
                .metadata("source", "ExecutionGraphBuilder")
                .metadata("detectedIntent", plan.detectedIntent().name())
                .metadata("nodeCount", nodes.size())
                .metadata("edgeCount", edges.size())
                .metadata("planConfidence", plan.confidence())
                .build();
    }

    /**
     * Collects the required capabilities in plan order, guaranteeing that
     * Identity is always present and listed first. Governance interceptors
     * (Safety, Validation, Observability) are intentionally omitted from the
     * graph and handled by the runtime executor's interceptor chain.
     */
    private static Map<CapabilityType, CapabilityRequirement.Priority>
            collectRequired(CapabilityPlan plan) {
        Map<CapabilityType, CapabilityRequirement.Priority> required =
                new LinkedHashMap<>();
        for (CapabilityRequirement requirement : plan.requiredCapabilities()) {
            required.putIfAbsent(requirement.capability(), requirement.priority());
        }
        // Rule: Identity must always be root.
        if (!required.containsKey(CapabilityType.IDENTITY)) {
            required.put(CapabilityType.IDENTITY, CapabilityRequirement.Priority.REQUIRED);
        }
        // Remove governance interceptors: they are NOT graph nodes.
        required.remove(CapabilityType.SAFETY);
        required.remove(CapabilityType.VALIDATION);
        required.remove(CapabilityType.OBSERVABILITY);
        return required;
    }

    // ========================================================================
    // Dependency generation
    // ========================================================================

    /**
     * Generates dependencies for every present capability according to the
     * graph rules. Each dependency list preserves deterministic order.
     */
    static Map<CapabilityType, List<CapabilityType>> computeDependencies(
            Set<CapabilityType> present) {
        Objects.requireNonNull(present, "present must not be null");
        Set<CapabilityType> safe = new LinkedHashSet<>(present);
        safe.add(CapabilityType.IDENTITY);

        Map<CapabilityType, List<CapabilityType>> dependencies = new LinkedHashMap<>();

        // Context depends on Identity.
        if (safe.contains(CapabilityType.CONTEXT)) {
            dependencies.put(CapabilityType.CONTEXT,
                    List.of(CapabilityType.IDENTITY));
        }

        // Knowledge depends on Context.
        if (safe.contains(CapabilityType.KNOWLEDGE)) {
            dependencies.put(CapabilityType.KNOWLEDGE,
                    List.of(first(safe, CapabilityType.CONTEXT, CapabilityType.IDENTITY)));
        }

        // Memory depends on Context.
        if (safe.contains(CapabilityType.MEMORY)) {
            dependencies.put(CapabilityType.MEMORY,
                    List.of(first(safe, CapabilityType.CONTEXT, CapabilityType.IDENTITY)));
        }

        // Reasoning depends on Knowledge and/or Memory.
        if (safe.contains(CapabilityType.REASONING)) {
            List<CapabilityType> preferred = presentN(safe,
                    CapabilityType.KNOWLEDGE, CapabilityType.MEMORY);
            List<CapabilityType> reasonDeps = preferred.isEmpty()
                    ? List.of(first(safe, CapabilityType.CONTEXT, CapabilityType.IDENTITY))
                    : preferred;
            dependencies.put(CapabilityType.REASONING, reasonDeps);
        }

        // Planning depends on Reasoning.
        if (safe.contains(CapabilityType.PLANNING)) {
            dependencies.put(CapabilityType.PLANNING,
                    List.of(first(safe, CapabilityType.REASONING,
                            CapabilityType.KNOWLEDGE, CapabilityType.MEMORY,
                            CapabilityType.CONTEXT, CapabilityType.IDENTITY)));
        }

        // Execution depends on Planning.
        if (safe.contains(CapabilityType.EXECUTION)) {
            dependencies.put(CapabilityType.EXECUTION,
                    List.of(first(safe, CapabilityType.PLANNING,
                            CapabilityType.REASONING, CapabilityType.KNOWLEDGE,
                            CapabilityType.MEMORY, CapabilityType.CONTEXT,
                            CapabilityType.IDENTITY)));
        }

        // Tools depend on Context.
        if (safe.contains(CapabilityType.TOOLS)) {
            dependencies.put(CapabilityType.TOOLS,
                    List.of(first(safe, CapabilityType.CONTEXT, CapabilityType.IDENTITY)));
        }

        // Modules depend on Reasoning.
        if (safe.contains(CapabilityType.MODELS)) {
            dependencies.put(CapabilityType.MODELS,
                    List.of(first(safe, CapabilityType.REASONING,
                            CapabilityType.CONTEXT, CapabilityType.IDENTITY)));
        }

        // Agents depend on Planning.
        if (safe.contains(CapabilityType.AGENTS)) {
            dependencies.put(CapabilityType.AGENTS,
                    List.of(first(safe, CapabilityType.PLANNING,
                            CapabilityType.REASONING, CapabilityType.KNOWLEDGE,
                            CapabilityType.MEMORY, CapabilityType.CONTEXT,
                            CapabilityType.IDENTITY)));
        }

        // Orchestration depends on the terminal node of the main chain.
        if (safe.contains(CapabilityType.ORCHESTRATION)) {
            dependencies.put(CapabilityType.ORCHESTRATION,
                    List.of(first(safe, CapabilityType.EXECUTION,
                            CapabilityType.PLANNING, CapabilityType.REASONING,
                            CapabilityType.KNOWLEDGE, CapabilityType.MEMORY,
                            CapabilityType.CONTEXT, CapabilityType.IDENTITY)));
        }

        return dependencies;
    }
        // ====================================================================
        // Node and edge construction
        // ========================================================================

    /**
     * Builds an {@link ExecutionNode} for a capability with the generated
     * dependency node IDs.
     */
    private static ExecutionNode buildNode(
            CapabilityType capability,
            CapabilityRequirement.Priority priority,
            List<CapabilityType> dependencies) {
        ExecutionNode.Builder builder = ExecutionNode.builder()
                .nodeId(capability.name())
                .capability(capability)
                .nodeType(NodeType.forCapability(capability))
                .priority(priority)
                .state(ExecutionNode.State.PENDING);
        for (CapabilityType dependency : dependencies) {
            builder.addDependency(dependency.name());
        }
        return builder.build();
    }

    /**
     * Generates one {@link ExecutionEdge} per dependency, plus observability
     * edges for every observed node. Edge IDs are deterministic.
     */
    private static List<ExecutionEdge> buildEdges(
            Map<CapabilityType, List<CapabilityType>> dependencyMap) {
        List<ExecutionEdge> edges = new ArrayList<>();
        for (Map.Entry<CapabilityType, List<CapabilityType>> entry
                : dependencyMap.entrySet()) {
            CapabilityType source = entry.getKey();
            for (CapabilityType target : entry.getValue()) {
                edges.add(ExecutionEdge.builder()
                        .edgeId("edge-" + source.name() + "-" + target.name())
                        .sourceNodeId(source.name())
                        .targetNodeId(target.name())
                        .reason(source + " depends on " + target)
                        .build());
            }
        }
        return List.copyOf(edges);
    }

    // ========================================================================
    // Selection helpers
    // ========================================================================

    /**
     * Returns the first candidate present in the set, or Identity which is
     * always guaranteed to be present.
     */
    private static CapabilityType first(
            Set<CapabilityType> safe, CapabilityType... candidates) {
        for (CapabilityType candidate : candidates) {
            if (safe.contains(candidate)) {
                return candidate;
            }
        }
        // Identity is always present, so this is unreachable in practice.
        return CapabilityType.IDENTITY;
    }

    /**
     * Returns all present candidates among the preferred list, in order.
     */
    private static List<CapabilityType> presentN(
            Set<CapabilityType> safe, CapabilityType... candidates) {
        List<CapabilityType> present = new ArrayList<>();
        for (CapabilityType candidate : candidates) {
            if (safe.contains(candidate)) {
                present.add(candidate);
            }
        }
        return present;
    }
}