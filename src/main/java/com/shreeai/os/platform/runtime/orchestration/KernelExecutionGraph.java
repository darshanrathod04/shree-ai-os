package com.shreeai.os.platform.runtime.orchestration;

import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult.KernelType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>KernelExecutionGraph</b>
 *
 * <p>Deterministic execution graph that defines the ordered sequence of kernels
 * to execute for a multi-kernel request. The graph preserves execution order and
 * dependencies between kernels.</p>
 *
 * <p><b>Ordering rules:</b></p>
 * <ul>
 *   <li>MEMORY_STORE → PLANNING (store context before planning)</li>
 *   <li>MEMORY_STORE → KNOWLEDGE (store context before knowledge retrieval)</li>
 *   <li>MEMORY_STORE → EXECUTION (store context before execution)</li>
 *   <li>KNOWLEDGE → PLANNING (knowledge grounds the plan)</li>
 *   <li>KNOWLEDGE → EXECUTION (knowledge informs execution)</li>
 *   <li>PLANNING → EXECUTION (plan precedes execution)</li>
 *   <li>Any → REFLECTION (reflection runs after everything)</li>
 * </ul>
 *
 * <p>This is an internal runtime model — not exposed in the public SDK.</p>
 *
 * @since Sprint-12
 */
public final class KernelExecutionGraph {

    private final List<Node> executionOrder;
    private final Map<KernelType, Node> nodes;
    private final Instant createdAt;

    private KernelExecutionGraph(List<Node> executionOrder, Map<KernelType, Node> nodes) {
        this.executionOrder = List.copyOf(executionOrder);
        this.nodes = Map.copyOf(nodes);
        this.createdAt = Instant.now();
    }

    /** Creates a new graph via the builder. */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return the ordered list of kernel nodes to execute
     */
    public List<Node> executionOrder() {
        return executionOrder;
    }

    /**
     * @return the node map keyed by kernel type
     */
    public Map<KernelType, Node> nodes() {
        return nodes;
    }

    /**
     * @return when this graph was created
     */
    public Instant createdAt() {
        return createdAt;
    }

    /**
     * @return the number of kernels in this graph
     */
    public int size() {
        return executionOrder.size();
    }

    @Override
    public String toString() {
        return "KernelExecutionGraph{"
                + "executionOrder=" + executionOrder.stream()
                .map(n -> n.kernelType().name())
                .toList()
                + ", size=" + size()
                + '}';
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Node
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * A single node in the execution graph representing one kernel execution.
     */
    public static final class Node {

        private final KernelType kernelType;
        private final int position;
        private final List<KernelType> dependsOn;
        private final Map<String, Object> context;
        private boolean executed;
        private Object result;

        public Node(
                KernelType kernelType,
                int position,
                List<KernelType> dependsOn,
                Map<String, Object> context
        ) {
            this.kernelType = Objects.requireNonNull(kernelType);
            this.position = position;
            this.dependsOn = List.copyOf(dependsOn);
            this.context = Map.copyOf(context);
            this.executed = false;
        }

        public KernelType kernelType() {
            return kernelType;
        }

        public int position() {
            return position;
        }

        public List<KernelType> dependsOn() {
            return dependsOn;
        }

        public Map<String, Object> context() {
            return context;
        }

        public boolean isExecuted() {
            return executed;
        }

        public void markExecuted(Object result) {
            this.executed = true;
            this.result = result;
        }

        public Object result() {
            return result;
        }

        /**
         * @return the intent type that triggers this kernel
         */
        public IntentAnalysisResult.IntentType intentType() {
            return switch (kernelType) {
                case MEMORY -> IntentAnalysisResult.IntentType.MEMORY_STORE;
                case PLANNING -> IntentAnalysisResult.IntentType.PLANNING;
                case KNOWLEDGE -> IntentAnalysisResult.IntentType.KNOWLEDGE_QUERY;
                case EXECUTION -> IntentAnalysisResult.IntentType.EXECUTION;
                case REFLECTION -> IntentAnalysisResult.IntentType.REFLECTION;
                case DEVELOPER -> IntentAnalysisResult.IntentType.DEVELOPER; // Sprint-14
                case CHIEF -> IntentAnalysisResult.IntentType.CHAT;
            };
        }

        @Override
        public String toString() {
            return "Node{kernel=" + kernelType + ", position=" + position
                    + ", dependsOn=" + dependsOn + ", executed=" + executed + '}';
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Builder
    // ─────────────────────────────────────────────────────────────────────────

    public static final class Builder {
        private final List<Node> nodes = new ArrayList<>();

        /**
         * Builds a graph from the intent analysis result.
         *
         * @param analysis the intent analysis result (must not be null)
         * @return a new KernelExecutionGraph
         */
        public KernelExecutionGraph buildFrom(IntentAnalysisResult analysis) {
            Objects.requireNonNull(analysis, "analysis must not be null");

            List<KernelType> required = analysis.requiredKernels();
            List<IntentAnalysisResult.IntentType> secondaries = analysis.secondaryIntents();

            if (required.isEmpty()) {
                // Single chief kernel
                addKernel(KernelType.CHIEF, 0, List.of(), Map.of("intent", IntentAnalysisResult.IntentType.CHAT));
                return build();
            }

            if (required.size() == 1) {
                // Single kernel
                KernelType single = required.get(0);
                addKernel(single, 0, List.of(),
                        Map.of("intent", primaryIntentFor(single, analysis)));
                return build();
            }

            // Multi-kernel: determine deterministic order
            buildMultiKernelGraph(required, secondaries, analysis);
            return build();
        }

        private void buildMultiKernelGraph(
                List<KernelType> required,
                List<IntentAnalysisResult.IntentType> secondaries,
                IntentAnalysisResult analysis
        ) {
            int position = 0;

            // Phase 1: MEMORY_STORE always first (context must be saved before anything else)
            if (required.contains(KernelType.MEMORY)) {
                addKernel(KernelType.MEMORY, position++, List.of(),
                        Map.of("intent", IntentAnalysisResult.IntentType.MEMORY_STORE,
                                "originalInput", analysis.originalInput()));
            }

            // Phase 2: KNOWLEDGE (ground subsequent planning/execution)
            if (required.contains(KernelType.KNOWLEDGE)) {
                List<KernelType> deps = hasMemory(required)
                        ? List.of(KernelType.MEMORY)
                        : List.of();
                addKernel(KernelType.KNOWLEDGE, position++, deps,
                        Map.of("intent", IntentAnalysisResult.IntentType.KNOWLEDGE_QUERY,
                                "originalInput", analysis.originalInput()));
            }

            // Phase 3: PLANNING (uses memory context and/or knowledge grounding)
            if (required.contains(KernelType.PLANNING)) {
                List<KernelType> deps = buildPlanningDependencies(required);
                addKernel(KernelType.PLANNING, position++, deps,
                        Map.of("intent", IntentAnalysisResult.IntentType.PLANNING,
                                "originalInput", analysis.originalInput(),
                                "entities", analysis.entities()));
            }

            // Phase 4: EXECUTION (plan precedes execution)
            if (required.contains(KernelType.EXECUTION)) {
                List<KernelType> deps = new ArrayList<>();
                if (required.contains(KernelType.PLANNING)) {
                    deps.add(KernelType.PLANNING);
                }
                if (required.contains(KernelType.KNOWLEDGE)) {
                    deps.add(KernelType.KNOWLEDGE);
                }
                addKernel(KernelType.EXECUTION, position++, deps,
                        Map.of("intent", IntentAnalysisResult.IntentType.EXECUTION,
                                "originalInput", analysis.originalInput()));
            }

            // Phase 5: REFLECTION always last (after all kernel executions)
            if (required.contains(KernelType.REFLECTION)) {
                List<KernelType> deps = new ArrayList<>(required);
                deps.remove(KernelType.REFLECTION);
                addKernel(KernelType.REFLECTION, position, deps,
                        Map.of("intent", IntentAnalysisResult.IntentType.REFLECTION));
            }
        }

        private boolean hasMemory(List<KernelType> required) {
            return required.contains(KernelType.MEMORY);
        }

        private List<KernelType> buildPlanningDependencies(List<KernelType> required) {
            List<KernelType> deps = new ArrayList<>();
            if (required.contains(KernelType.MEMORY)) {
                deps.add(KernelType.MEMORY);
            }
            if (required.contains(KernelType.KNOWLEDGE)) {
                deps.add(KernelType.KNOWLEDGE);
            }
            return deps;
        }

        private IntentAnalysisResult.IntentType primaryIntentFor(
                KernelType kernel,
                IntentAnalysisResult analysis
        ) {
            return switch (kernel) {
                case MEMORY -> IntentAnalysisResult.IntentType.MEMORY_STORE;
                case PLANNING -> IntentAnalysisResult.IntentType.PLANNING;
                case KNOWLEDGE -> IntentAnalysisResult.IntentType.KNOWLEDGE_QUERY;
                case EXECUTION -> IntentAnalysisResult.IntentType.EXECUTION;
                case REFLECTION -> IntentAnalysisResult.IntentType.REFLECTION;
                case DEVELOPER -> IntentAnalysisResult.IntentType.DEVELOPER; // Sprint-14
                case CHIEF -> IntentAnalysisResult.IntentType.CHAT;
            };
        }

        public Builder addKernel(
                KernelType kernelType,
                int position,
                List<KernelType> dependsOn,
                Map<String, Object> context
        ) {
            nodes.add(new Node(kernelType, position, dependsOn, context));
            return this;
        }

        public KernelExecutionGraph build() {
            Map<KernelType, Node> nodeMap = new LinkedHashMap<>();
            for (Node node : nodes) {
                nodeMap.put(node.kernelType(), node);
            }
            return new KernelExecutionGraph(nodes, nodeMap);
        }
    }
}
