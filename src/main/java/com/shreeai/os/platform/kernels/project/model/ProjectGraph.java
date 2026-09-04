package com.shreeai.os.platform.kernels.project.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <b>ProjectGraph</b> — in-memory, queryable architecture graph.
 *
 * <p>Stores nodes (classes) and edges (dependencies) produced by the
 * project parser. Supports forward/reverse lookups, impact analysis,
 * and cycle detection.</p>
 */
public final class ProjectGraph {

    private final Map<String, ProjectClass> classesByFqn;
    private final List<ProjectDependency> edges;
    private final Map<String, List<ProjectDependency>> outgoing;
    private final Map<String, List<ProjectDependency>> incoming;

    public ProjectGraph(List<ProjectClass> classes, List<ProjectDependency> edges) {
        this.classesByFqn = new HashMap<>();
        for (ProjectClass c : classes) {
            this.classesByFqn.put(c.fullyQualifiedName(), c);
        }
        this.edges = List.copyOf(Objects.requireNonNull(edges));
        this.outgoing = new HashMap<>();
        this.incoming = new HashMap<>();
        for (ProjectDependency edge : edges) {
            outgoing.computeIfAbsent(edge.source(), k -> new ArrayList<>()).add(edge);
            incoming.computeIfAbsent(edge.target(), k -> new ArrayList<>()).add(edge);
        }
    }

    public List<ProjectClass> classes() {
        return List.copyOf(classesByFqn.values());
    }

    public List<ProjectDependency> edges() {
        return edges;
    }

    public ProjectClass findClass(String fqn) {
        if (fqn == null) return null;
        return classesByFqn.get(fqn);
    }

    public List<ProjectClass> findByName(String simpleName) {
        if (simpleName == null || simpleName.isBlank()) return List.of();
        List<ProjectClass> matches = new ArrayList<>();
        for (ProjectClass c : classesByFqn.values()) {
            if (simpleName.equals(c.name())) {
                matches.add(c);
            }
        }
        return matches;
    }

    public List<ProjectDependency> outgoing(String fqn) {
        return outgoing.getOrDefault(fqn, List.of());
    }

    public List<ProjectDependency> incoming(String fqn) {
        return incoming.getOrDefault(fqn, List.of());
    }

    /**
     * Returns the set of classes transitively affected by modifying
     * the given class. Walks incoming edges.
     */
    public List<String> impactOf(String fqn) {
        if (!classesByFqn.containsKey(fqn)) return List.of();
        Set<String> visited = new LinkedHashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(fqn);
        while (!stack.isEmpty()) {
            String current = stack.pop();
            for (ProjectDependency edge : incoming(current)) {
                if (visited.add(edge.source())) {
                    stack.push(edge.source());
                }
            }
        }
        // Return in deterministic order
        List<String> result = new ArrayList<>(visited);
        Collections.sort(result);
        return result;
    }

    /**
     * Detects cycles in the dependency graph using DFS. Returns a list
     * of cycles (each cycle as a list of FQNs). Empty if acyclic.
     */
    public List<List<String>> detectCycles() {
        Map<String, Integer> color = new HashMap<>(); // 0=white, 1=gray, 2=black
        List<List<String>> cycles = new ArrayList<>();
        List<String> path = new ArrayList<>();

        for (String node : classesByFqn.keySet()) {
            dfsCycle(node, color, path, cycles);
        }
        return cycles;
    }

    private void dfsCycle(String node, Map<String, Integer> color,
                          List<String> path, List<List<String>> cycles) {
        Integer c = color.getOrDefault(node, 0);
        if (c == 1) {
            // Found a back-edge → cycle
            int startIdx = path.indexOf(node);
            if (startIdx >= 0) {
                cycles.add(new ArrayList<>(path.subList(startIdx, path.size())));
            }
            return;
        }
        if (c == 2) return;
        color.put(node, 1);
        path.add(node);
        for (ProjectDependency edge : outgoing(node)) {
            dfsCycle(edge.target(), color, path, cycles);
        }
        path.remove(path.size() - 1);
        color.put(node, 2);
    }

    public int size() {
        return classesByFqn.size();
    }

    public int edgeCount() {
        return edges.size();
    }
}
