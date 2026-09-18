package com.shreeai.os.platform.kernels.inference.engine;

import com.shreeai.os.platform.kernels.inference.model.AlternativeCandidate;
import com.shreeai.os.platform.kernels.inference.model.AlternativeSet;
import com.shreeai.os.platform.kernels.inference.model.AlternativeStep;
import com.shreeai.os.platform.kernels.inference.model.AlternativeType;
import com.shreeai.os.platform.kernels.reasoning.model.CausalGraph;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesisGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationStatus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * <b>DefaultAlternativeGenerationEngine</b>
 *
 * <p>The I1 implementation of {@link AlternativeGenerationEngine}: a
 * stateless, thread-safe, fully deterministic generator of executable
 * solution alternatives from verified knowledge.</p>
 *
 * <p><b>Stage 1 - Verified concepts:</b> concept nodes of the
 * {@link ReasoningGraph} whose {@link VerificationGraph} status is
 * {@link VerificationStatus#VERIFIED}, in lexicographic base order. When no
 * verified concept exists, the deterministic statements of the
 * {@link SynthesisGraph} (verified trusted-evidence facts) are the fallback.
 * The raw prompt is never read.</p>
 *
 * <p><b>Stage 2 - Dependency DAG:</b> every causal cause→effect edge whose
 * endpoint titles are both verified concepts becomes a prerequisite edge
 * (learn the cause before the effect). Nodes outside the verified concept set
 * are ignored.</p>
 *
 * <p><b>Stage 3 - Strategy variants</b> (at most five, one per type):</p>
 * <ul>
 *   <li>{@code SEQUENTIAL} - deterministic topological order (Kahn's algorithm
 *       with lexicographic tie-breaking).</li>
 *   <li>{@code ACCELERATED} - concepts grouped by dependency level; each level
 *       becomes one merged step (titles joined with {@code " + "} in
 *       lexicographic order) - independent branches merge.</li>
 *   <li>{@code PRACTICAL} - project-first: the foundation concept, a
 *       {@code "Mini Project"} practice anchor, the remaining concepts in
 *       topological order, and a closing {@code "Portfolio"} anchor.</li>
 *   <li>{@code THEORETICAL} - concepts ordered by dependent count descending
 *       (foundation concepts that unlock the most come first), lexicographic
 *       tie-break.</li>
 *   <li>{@code BALANCED} - even positions from the sequential order, odd
 *       positions from the theoretical order, first occurrence kept.</li>
 * </ul>
 *
 * <p><b>Stage 4 - Feasibility:</b> {@code completedPrerequisites / totalSteps}
 * where a step is completed when every prerequisite of every concept it covers
 * appears in strictly earlier steps. Clamped to {@code [0,1]}.</p>
 *
 * <p><b>Stage 5 - Stable ordering:</b> the locked output order BALANCED,
 * SEQUENTIAL, PRACTICAL, ACCELERATED, THEORETICAL. Never reordered by
 * feasibility - ranking belongs to I2.</p>
 *
 * <p><b>Ownership:</b> Inference Kernel - I1 Alternative Generation</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class DefaultAlternativeGenerationEngine implements AlternativeGenerationEngine {

    /** Locked output order - never score-derived. */
    private static final List<AlternativeType> OUTPUT_ORDER = List.of(
            AlternativeType.BALANCED,
            AlternativeType.SEQUENTIAL,
            AlternativeType.PRACTICAL,
            AlternativeType.ACCELERATED,
            AlternativeType.THEORETICAL);

    @Override
    public AlternativeSet generate(ReasoningGraph reasoningGraph,
                                   SynthesisGraph synthesisGraph,
                                   CausalGraph causalGraph,
                                   VerificationGraph verificationGraph) {
        // Stage 1: collect verified concepts only.
        List<String> concepts =
                verifiedConcepts(reasoningGraph, synthesisGraph, verificationGraph);
        if (concepts.isEmpty()) {
            return AlternativeSet.empty();
        }

        // Stage 2: build the prerequisite DAG from the causal graph.
        Map<String, Set<String>> prerequisites =
                prerequisiteMap(concepts, causalGraph);

        List<String> sequentialOrder = topologicalOrder(concepts, prerequisites);
        List<String> theoreticalOrder = theoreticalOrder(concepts, prerequisites);
        Map<String, Integer> levels = dependencyLevels(sequentialOrder, prerequisites);

        // Stage 3: generate the deterministic strategy variants.
        List<AlternativeCandidate> generated = new ArrayList<>();
        for (AlternativeType type : OUTPUT_ORDER) {
            List<AlternativeStep> steps = switch (type) {
                case SEQUENTIAL -> sequentialSteps(sequentialOrder);
                case ACCELERATED -> acceleratedSteps(sequentialOrder, levels);
                case PRACTICAL -> practicalSteps(sequentialOrder);
                case THEORETICAL -> theoreticalSteps(theoreticalOrder);
                case BALANCED -> balancedSteps(sequentialOrder, theoreticalOrder);
            };
            generated.add(candidate(type, steps, prerequisites));
        }

        // Stage 5: locked stable output order - never reordered by score.
        return new AlternativeSet(List.copyOf(generated));
    }

    /** Stage 1: verified concept titles, lexicographic base order. */
    private static List<String> verifiedConcepts(ReasoningGraph reasoningGraph,
                                                 SynthesisGraph synthesisGraph,
                                                 VerificationGraph verificationGraph) {
        if (reasoningGraph == null) {
            return List.of();
        }
        Set<String> verifiedNodeIds = new TreeSet<>();
        if (verificationGraph != null) {
            verificationGraph.nodes().stream()
                    .filter(node -> node.status() == VerificationStatus.VERIFIED)
                    .forEach(node -> verifiedNodeIds.add(node.nodeId()));
        }
        TreeSet<String> concepts = new TreeSet<>();
        reasoningGraph.nodes().stream()
                .filter(node -> node.type() == com.shreeai.os.platform.kernels.reasoning.model.ReasoningNodeType.CONCEPT)
                .filter(node -> verificationGraph == null || verifiedNodeIds.contains(node.nodeId()))
                .forEach(node -> concepts.add(node.title()));
        if (concepts.isEmpty() && synthesisGraph != null) {
            // Deterministic fallback: verified facts from trusted evidence.
            synthesisGraph.facts().forEach(fact -> concepts.add(fact.statement()));
        }
        return List.copyOf(concepts);
    }

    /** Stage 2: title -> set of prerequisite titles, from causal cause→effect edges. */
    private static Map<String, Set<String>> prerequisiteMap(List<String> concepts,
                                                            CausalGraph causalGraph) {
        Set<String> conceptSet = new TreeSet<>(concepts);
        Map<String, String> titleOf = new HashMap<>();
        Map<String, Set<String>> prerequisites = new HashMap<>();
        for (String concept : concepts) {
            prerequisites.put(concept, new TreeSet<>());
        }
        if (causalGraph == null) {
            return prerequisites;
        }
        causalGraph.nodes().forEach(node -> titleOf.put(node.nodeId(), node.title()));
        causalGraph.edges().forEach(edge -> {
            String fromTitle = titleOf.get(edge.fromNode());
            String toTitle = titleOf.get(edge.toNode());
            if (fromTitle == null || toTitle == null) {
                return;
            }
            if (conceptSet.contains(fromTitle) && conceptSet.contains(toTitle)
                    && !fromTitle.equals(toTitle)) {
                prerequisites.get(toTitle).add(fromTitle);
            }
        });
        return prerequisites;
    }

    /**
     * Deterministic topological order (Kahn's algorithm with lexicographic
     * tie-breaking). Cycle leftovers are appended lexicographically so the
     * result stays total and deterministic.
     */
    private static List<String> topologicalOrder(List<String> concepts,
                                                 Map<String, Set<String>> prerequisites) {
        Map<String, Integer> remaining = new HashMap<>();
        Map<String, Set<String>> dependents = new HashMap<>();
        for (String concept : concepts) {
            remaining.put(concept, prerequisites.get(concept).size());
            dependents.put(concept, new TreeSet<>());
        }
        for (Map.Entry<String, Set<String>> entry : prerequisites.entrySet()) {
            for (String prerequisite : entry.getValue()) {
                dependents.get(prerequisite).add(entry.getKey());
            }
        }
        PriorityQueue<String> ready = new PriorityQueue<>();
        for (Map.Entry<String, Integer> entry : remaining.entrySet()) {
            if (entry.getValue() == 0) {
                ready.add(entry.getKey());
            }
        }
        List<String> order = new ArrayList<>(concepts.size());
        while (!ready.isEmpty()) {
            String current = ready.poll();
            order.add(current);
            for (String dependent : dependents.get(current)) {
                int left = remaining.merge(dependent, -1, Integer::sum);
                if (left == 0) {
                    ready.add(dependent);
                }
            }
        }
        for (String concept : concepts) {
            if (!order.contains(concept)) {
                order.add(concept);
            }
        }
        return List.copyOf(order);
    }

    /** Dependency level per concept: 0 for roots, else 1 + max level of prerequisites. */
    /** SEQUENTIAL: strict prerequisite order, one concept per step. */
    private static List<AlternativeStep> sequentialSteps(List<String> order) {
        return stepsOf(order);
    }

    /**
     * ACCELERATED: merge independent branches - one step per dependency level,
     * titles joined with {@code " + "} in lexicographic order.
     */
    private static List<AlternativeStep> acceleratedSteps(List<String> order,
                                                          Map<String, Integer> levels) {
        TreeMap<Integer, List<String>> byLevel = new TreeMap<>();
        for (String concept : order) {
            byLevel.computeIfAbsent(levels.getOrDefault(concept, 0),
                    key -> new ArrayList<>()).add(concept);
        }
        List<String> merged = new ArrayList<>();
        for (List<String> levelConcepts : byLevel.values()) {
            merged.add(String.join(" + ", levelConcepts));
        }
        return stepsOf(merged);
    }

    /**
     * PRACTICAL: project-first path - foundation concept, practice anchor,
     * remaining concepts in topological order, closing portfolio anchor.
     */
    private static List<AlternativeStep> practicalSteps(List<String> order) {
        List<String> path = new ArrayList<>();
        path.add(order.get(0));
        if (order.size() >= 3) {
            path.add(PRACTICE_ANCHOR);
            path.addAll(order.subList(1, order.size()));
            path.add(PORTFOLIO_ANCHOR);
        } else {
            path.addAll(order.subList(1, order.size()));
        }
        return stepsOf(path);
    }

    /** THEORETICAL: concept-heavy ordering - highest dependent count first. */
    private static List<AlternativeStep> theoreticalSteps(List<String> theoreticalOrder) {
        return stepsOf(theoreticalOrder);
    }

    /**
     * BALANCED: even positions from the sequential order, odd positions from
     * the theoretical order, first occurrence kept.
     */
    private static List<AlternativeStep> balancedSteps(List<String> sequentialOrder,
                                                       List<String> theoreticalOrder) {
        LinkedHashSet<String> mixed = new LinkedHashSet<>();
        for (int i = 0; i < Math.max(sequentialOrder.size(), theoreticalOrder.size()); i++) {
            if (i < sequentialOrder.size()) {
                mixed.add(sequentialOrder.get(i));
            }
            if (i < theoreticalOrder.size()) {
                mixed.add(theoreticalOrder.get(i));
            }
        }
        return stepsOf(List.copyOf(mixed));
    }

    /** THEORETICAL order: dependent count descending, lexicographic tie-break. */
    private static List<String> theoreticalOrder(List<String> concepts,
                                                 Map<String, Set<String>> prerequisites) {
        Map<String, Integer> dependentCount = new HashMap<>();
        for (Set<String> prereqs : prerequisites.values()) {
            for (String prerequisite : prereqs) {
                dependentCount.merge(prerequisite, 1, Integer::sum);
            }
        }
        List<String> order = new ArrayList<>(concepts);
        order.sort((a, b) -> {
            int byDependents = Integer.compare(
                    dependentCount.getOrDefault(b, 0), dependentCount.getOrDefault(a, 0));
            return byDependents != 0 ? byDependents : a.compareTo(b);
        });
        return List.copyOf(order);
    }

    /** Builds one-based, sequentially ordered steps from the path titles. */
    private static List<AlternativeStep> stepsOf(List<String> titles) {
        List<AlternativeStep> steps = new ArrayList<>(titles.size());
        for (int i = 0; i < titles.size(); i++) {
            steps.add(new AlternativeStep(i + 1, titles.get(i)));
        }
        return List.copyOf(steps);
    }

    /** Practice anchors of the PRACTICAL project-first path. */
    private static final String PRACTICE_ANCHOR = "Mini Project";
    private static final String PORTFOLIO_ANCHOR = "Portfolio";

    /** Dependency level per concept: 0 for roots, else 1 + max prerequisite level. */
    private static Map<String, Integer> dependencyLevels(List<String> topologicalOrder,
                                                         Map<String, Set<String>> prerequisites) {
        Map<String, Integer> levels = new HashMap<>();
        for (String concept : topologicalOrder) {
            int level = 0;
            for (String prerequisite : prerequisites.get(concept)) {
                Integer prerequisiteLevel = levels.get(prerequisite);
                if (prerequisiteLevel != null) {
                    level = Math.max(level, prerequisiteLevel + 1);
                }
            }
            levels.put(concept, level);
        }
        return levels;
    }

    /**
     * Stage 4 + identity: builds one candidate with the locked deterministic
     * feasibility ({@code completedPrerequisites / totalSteps}) and the
     * SHA-256 deterministic candidate id.
     */
    private static AlternativeCandidate candidate(AlternativeType type,
                                                  List<AlternativeStep> steps,
                                                  Map<String, Set<String>> prerequisites) {
        Set<String> satisfied = new TreeSet<>();
        int completed = 0;
        for (AlternativeStep step : steps) {
            Set<String> covered = coveredConcepts(step.title(), prerequisites.keySet());
            boolean complete = true;
            for (String concept : covered) {
                if (!satisfied.containsAll(prerequisites.getOrDefault(concept, Set.of()))) {
                    complete = false;
                    break;
                }
            }
            if (complete) {
                completed++;
            }
            satisfied.addAll(covered);
        }
        long total = steps.size();
        double feasibility = total == 0 ? 0.0 : Math.max(0.0, Math.min(1.0, (double) completed / total));
        String title = candidateTitle(type, steps.size());
        return new AlternativeCandidate(candidateId(type, title, steps), type, title, steps, feasibility);
    }

    /** The verified concepts covered by one step (practice anchors cover none). */
    private static Set<String> coveredConcepts(String title, Set<String> concepts) {
        Set<String> covered = new TreeSet<>();
        for (String concept : concepts) {
            if (title.equals(concept) || title.contains(concept)) {
                covered.add(concept);
            }
        }
        return covered;
    }

    /** Deterministic candidate title: strategy label plus step count. */
    private static String candidateTitle(AlternativeType type, int stepCount) {
        return switch (type) {
            case SEQUENTIAL -> "Sequential strategy (" + stepCount + " steps)";
            case ACCELERATED -> "Accelerated strategy (" + stepCount + " steps)";
            case PRACTICAL -> "Practical strategy (" + stepCount + " steps)";
            case THEORETICAL -> "Theoretical strategy (" + stepCount + " steps)";
            case BALANCED -> "Balanced strategy (" + stepCount + " steps)";
        };
    }

    /**
     * Deterministic candidate id: lowercase hex SHA-256 over
     * {@code type | title | ordered step titles}. Identical inputs always
     * produce identical ids - no UUID, no randomness.
     */
    private static String candidateId(AlternativeType type,
                                      String title,
                                      List<AlternativeStep> steps) {
        StringBuilder seed = new StringBuilder();
        seed.append(type.name()).append('|').append(title).append('|');
        for (AlternativeStep step : steps) {
            seed.append(step.title()).append("->");
        }
        return sha256Hex(seed.toString());
    }

    /** Lowercase hex SHA-256 of the UTF-8 encoded seed. */
    private static String sha256Hex(String seed) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest is unavailable", e);
        }
        byte[] hash = digest.digest(seed.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            hex.append(Character.forDigit((b >> 4) & 0xF, 16));
            hex.append(Character.forDigit((b >> 0) & 0xF, 16));
        }
        return hex.toString();
    }
}
