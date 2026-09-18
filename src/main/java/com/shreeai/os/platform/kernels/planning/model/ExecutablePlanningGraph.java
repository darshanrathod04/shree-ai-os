package com.shreeai.os.platform.kernels.planning.model;

import com.shreeai.os.platform.kernels.planning.error.PlanValidationException;
import com.shreeai.os.platform.kernels.planning.error.PlanningError;
import com.shreeai.os.platform.kernels.planning.error.PlanningErrorCode;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <b>ExecutablePlanningGraph</b>
 *
 * <p>The canonical workflow artifact produced by the P2.5 Executable
 * Planning Graph engine: an execution-ready projection of an
 * {@link ExecutionPlan} that Chief Intelligence will later orchestrate.</p>
 *
 * <p>The engine does <em>not</em> execute tasks - it only prepares
 * execution-ready state transitions. Every {@link ExecutionNode} is one
 * scheduled task; every {@link ExecutionEdge} is one task-graph dependency,
 * never an inferred one.</p>
 *
 * <p><b>Canonical ordering (stable forever):</b> nodes are ordered by day,
 * then task order, then title, then node identifier; edges are ordered by
 * predecessor identifier, then successor identifier. The same input always
 * produces a byte-identical artifact on every JVM, every run, and every
 * machine.</p>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Java 21 record - immutable by construction.</li>
 *   <li>Deeply immutable - lists are defensively copied and frozen; every
 *       element is itself an immutable record.</li>
 *   <li>Validated on construction - duplicate node identifiers, dangling
 *       edges, duplicate edges, and cycles are rejected deterministically.</li>
 *   <li>Data-only - contains no execution logic and no clocks.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel - P2.5 Executable Planning Graph</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param nodes the execution nodes in canonical order (never null; one per
 *              scheduled task)
 * @param edges the dependency edges in canonical order (never null; one per
 *              task-graph dependency)
 *
 * @since P2.5
 * @see ExecutionNode
 * @see ExecutionEdge
 */
public record ExecutablePlanningGraph(
        List<ExecutionNode> nodes,
        List<ExecutionEdge> edges) {

    /**
     * Creates a validated, deeply-immutable executable planning graph.
     *
     * @throws NullPointerException     if either list is null
     * @throws IllegalArgumentException if node identifiers are not unique, if
     *                                  an edge references an unknown node, or
     *                                  if an edge is duplicated
     * @throws PlanValidationException  if the graph contains a cycle
     */
    public ExecutablePlanningGraph {
        List<ExecutionNode> frozenNodes = List.copyOf(nodes);
        List<ExecutionEdge> frozenEdges = List.copyOf(edges);
        validateStructure(frozenNodes, frozenEdges);
        validateAcyclic(frozenNodes, frozenEdges);
        nodes = frozenNodes;
        edges = frozenEdges;
    }

    /**
     * Returns an empty executable planning graph.
     *
     * @return the shared empty graph (never null)
     */
    public static ExecutablePlanningGraph empty() {
        return new ExecutablePlanningGraph(List.of(), List.of());
    }

    /**
     * Creates a graph from possibly-null inputs, tolerating null for
     * backward compatibility and empty graphs.
     *
     * @param nodes the nodes (null and empty both yield an empty list)
     * @param edges the edges (null and empty both yield an empty list)
     * @return a validated, deeply-immutable graph (never null)
     */
    public static ExecutablePlanningGraph of(List<ExecutionNode> nodes, List<ExecutionEdge> edges) {
        return new ExecutablePlanningGraph(
                nodes == null ? List.of() : nodes,
                edges == null ? List.of() : edges);
    }

    /**
     * Validates the structural integrity of a candidate graph: unique node
     * identifiers, unique edges, and no dangling endpoint references.
     *
     * @param nodes the nodes to validate (must not be null)
     * @param edges the edges to validate (must not be null)
     * @throws IllegalArgumentException if any structural rule is violated
     */
    public static void validateStructure(List<ExecutionNode> nodes, List<ExecutionEdge> edges) {
        Set<String> seenIds = new HashSet<>();
        for (ExecutionNode node : nodes) {
            if (!seenIds.add(node.nodeId())) {
                throw new IllegalArgumentException(
                        "duplicate nodeId detected: " + node.nodeId());
            }
        }
        Set<String> seenEdges = new HashSet<>();
        List<String> dangling = new ArrayList<>();
        for (ExecutionEdge edge : edges) {
            if (!seenEdges.add(edge.edgeKey())) {
                throw new IllegalArgumentException(
                        "duplicate edge detected: " + edge.edgeKey());
            }
            if (!seenIds.contains(edge.fromNodeId())) {
                dangling.add(edge.fromNodeId());
            }
            if (!seenIds.contains(edge.toNodeId())) {
                dangling.add(edge.toNodeId());
            }
        }
        if (!dangling.isEmpty()) {
            List<String> sorted = new ArrayList<>(dangling);
            sorted.sort(String::compareTo);
            throw new IllegalArgumentException(
                    "edges reference unknown nodes: " + sorted);
        }
    }

    /**
     * Validates that the given nodes and edges form a Directed Acyclic
     * Graph, throwing {@link PlanValidationException} when a cycle exists.
     *
     * <p>Cycle detection is deterministic: the offending node identifiers
     * are reported in ascending lexicographic order.</p>
     *
     * @param nodes the nodes to validate (must not be null)
     * @param edges the edges to validate (must not be null)
     * @throws PlanValidationException if a cycle exists
     */
    public static void validateAcyclic(List<ExecutionNode> nodes, List<ExecutionEdge> edges) {
        List<String> cyclic = findCyclicNodeIds(nodes, edges);
        if (!cyclic.isEmpty()) {
            PlanningError error = new PlanningError(
                    PlanningErrorCode.VALIDATION_ERROR,
                    "Executable planning graph contains a cycle among nodes: " + cyclic,
                    Instant.now(),
                    Map.of("cyclicNodeIds", cyclic));
            throw new PlanValidationException(error);
        }
    }

    /**
     * Finds node identifiers participating in a directed cycle using an
     * iterative depth-first search with a deterministic three-color scheme.
     * Disconnected components are fully traversed.
     *
     * @param nodes the nodes (must not be null)
     * @param edges the edges (must not be null)
     * @return the offending identifiers in ascending lexicographic order
     *         (never null; empty when acyclic)
     */
    private static List<String> findCyclicNodeIds(List<ExecutionNode> nodes, List<ExecutionEdge> edges) {
        Map<String, List<String>> successors = new LinkedHashMap<>();
        for (ExecutionNode node : nodes) {
            successors.put(node.nodeId(), new ArrayList<>());
        }
        for (ExecutionEdge edge : edges) {
            successors.get(edge.fromNodeId()).add(edge.toNodeId());
        }
        final int white = 0;
        final int gray = 1;
        final int black = 2;
        Map<String, Integer> color = new LinkedHashMap<>();
        for (String id : successors.keySet()) {
            color.put(id, white);
        }
        Set<String> cyclic = new HashSet<>();
        for (String start : successors.keySet()) {
            if (color.get(start) != white) {
                continue;
            }
            Deque<String[]> stack = new ArrayDeque<>();
            Deque<String> path = new ArrayDeque<>();
            stack.push(new String[]{start, null});
            while (!stack.isEmpty()) {
                String[] frame = stack.peek();
                String current = frame[0];
                if (frame[1] == null) {
                    if (color.get(current) != white) {
                        stack.pop();
                        continue;
                    }
                    color.put(current, gray);
                    path.push(current);
                    frame[1] = "visited";
                    for (String next : successors.get(current)) {
                        if (color.get(next) == gray) {
                            collectCycle(path, next, cyclic);
                        } else if (color.get(next) == white) {
                            stack.push(new String[]{next, null});
                        }
                    }
                } else {
                    stack.pop();
                    color.put(current, black);
                    path.pop();
                }
            }
        }
        List<String> sorted = new ArrayList<>(cyclic);
        sorted.sort(String::compareTo);
        return List.copyOf(sorted);
    }

    /** Marks every node from the top of the path down to the cycle entry. */
    private static void collectCycle(Deque<String> path, String entry, Set<String> cyclic) {
        for (String id : path) {
            cyclic.add(id);
            if (id.equals(entry)) {
                break;
            }
        }
    }

    /**
     * Returns the node with the given identifier.
     *
     * @param nodeId the identifier to look up (must not be null)
     * @return the matching node (never null)
     * @throws IllegalArgumentException if no node carries the identifier
     */
    public ExecutionNode nodeById(String nodeId) {
        for (ExecutionNode node : nodes) {
            if (node.nodeId().equals(nodeId)) {
                return node;
            }
        }
        throw new IllegalArgumentException("unknown nodeId: " + nodeId);
    }

    /**
     * Returns the node corresponding to the given planning task identifier.
     *
     * @param taskId the planning task identifier (must not be null)
     * @return the matching node (never null)
     * @throws IllegalArgumentException if no node carries the task identifier
     */
    public ExecutionNode nodeByTaskId(String taskId) {
        for (ExecutionNode node : nodes) {
            if (node.taskId().equals(taskId)) {
                return node;
            }
        }
        throw new IllegalArgumentException("unknown taskId: " + taskId);
    }

    @Override
    public String toString() {
        return "ExecutablePlanningGraph{nodes=" + nodes.size()
                + ", edges=" + edges.size() + "}";
    }
}
