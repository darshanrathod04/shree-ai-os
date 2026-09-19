package com.shreeai.os.platform.kernels.planning.model;

import com.shreeai.os.platform.kernels.planning.error.PlanValidationException;
import com.shreeai.os.platform.kernels.planning.error.PlanningError;
import com.shreeai.os.platform.kernels.planning.error.PlanningErrorCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * <b>TaskGraph</b>
 *
 * <p>The canonical, executable Task Dependency Graph artifact that succeeds
 * a {@link PlanBlueprint} and precedes resource &amp; time allocation. It is
 * the first true scheduling DAG inside the Planning Kernel.</p>
 *
 * <p><b>Locking invariants</b> (enforced by the compact constructor):</p>
 * <ul>
 *   <li>Both component lists are non-null and defensively copied into
 *       immutable copies.</li>
 *   <li>No two tasks share a {@code taskId} - identifiers are unique.</li>
 *   <li>Every {@link TaskDependency} references two existing task
 *       identifiers - no dangling edges.</li>
 *   <li>No duplicate edges exist.</li>
 *   <li>The graph is a Directed Acyclic Graph - a cycle throws
 *       {@link PlanValidationException} at construction time.</li>
 * </ul>
 *
 * <p><b>Determinism:</b> cycle detection, topological ordering, and
 * canonical ordering are all implemented with iteration (no recursion, so
 * no stack-overflow on large graphs). Ordering tie-breaks always include
 * {@code taskId} as the final total discriminator, so the same graph is
 * sorted into the identical sequence on every JVM.</p>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Java 21 record - immutable by construction.</li>
 *   <li>Deeply immutable - every component and every list element is
 *       immutable; the record stores {@code List.copyOf} views.</li>
 *   <li>No recursion overflow - Kahn's algorithm with an explicit queue.</li>
 *   <li>No graph library - plain {@code Map} and {@code PriorityQueue}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel - P2.2 Task Dependency Graph</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param tasks        the immutable set of planning tasks
 * @param dependencies the immutable set of precedence edges
 *
 * @since P2.2
 * @see PlanningTask
 * @see TaskDependency
 */
public record TaskGraph(
        List<PlanningTask> tasks,
        List<TaskDependency> dependencies) {

    /** An empty task graph with no tasks and no dependencies. */
    public static final TaskGraph EMPTY = new TaskGraph(List.of(), List.of());

    /**
     * Compact constructor: validates and freezes the graph.
     *
     * @throws NullPointerException     if either list, any element, or any
     *                                  identifier component is null
     * @throws IllegalArgumentException if identifiers are duplicated, edges
     *                                  are duplicated, or an edge references
     *                                  an unknown task
     * @throws PlanValidationException  if the dependency graph contains a
     *                                  cycle
     */
    public TaskGraph {
        List<PlanningTask> frozenTasks = tasks == null
                ? List.of() : List.copyOf(tasks);
        List<TaskDependency> frozenDeps = dependencies == null
                ? List.of() : List.copyOf(dependencies);
        validateStructure(frozenTasks, frozenDeps);
        validateAcyclic(frozenTasks, frozenDeps);
        tasks = frozenTasks;
        dependencies = frozenDeps;
    }

    /**
     * Returns an empty task graph.
     *
     * @return the shared empty graph (never null)
     */
    public static TaskGraph empty() {
        return EMPTY;
    }

    /**
     * Creates a task graph from possibly-null inputs, tolerating null for
     * backward compatibility and empty graphs.
     *
     * @param tasks        the tasks (null and empty both yield an empty list)
     * @param dependencies the dependencies (null and empty both yield an empty list)
     * @return a validated, deeply-immutable task graph (never null)
          */
    public static TaskGraph of(List<PlanningTask> tasks, List<TaskDependency> dependencies) {
        return new TaskGraph(tasks, dependencies);
    }

    /**
     * Validates the structural integrity of a candidate graph: unique task
     * identifiers, unique edges, and no dangling endpoint references.
     *
     * @param tasks        the tasks to validate (must not be null)
     * @param dependencies the dependencies to validate (must not be null)
     * @throws IllegalArgumentException if any structural rule is violated
     */
    public static void validateStructure(List<PlanningTask> tasks, List<TaskDependency> dependencies) {
        Set<String> seenIds = new HashSet<>();
        for (PlanningTask task : tasks) {
            Objects.requireNonNull(task, "task must not be null");
            if (!seenIds.add(task.taskId())) {
                throw new IllegalArgumentException(
                        "duplicate taskId detected: " + task.taskId());
            }
        }
        Set<String> knownIds = new LinkedHashSet<>(seenIds);
        Set<String> seenEdges = new HashSet<>();
        List<String> dangling = new ArrayList<>();
        for (TaskDependency dep : dependencies) {
            Objects.requireNonNull(dep, "dependency must not be null");
            if (!seenEdges.add(dep.edgeKey())) {
                throw new IllegalArgumentException(
                        "duplicate edge detected: " + dep.edgeKey());
            }
            if (!knownIds.contains(dep.fromTaskId())) {
                dangling.add(dep.fromTaskId());
            }
            if (!knownIds.contains(dep.toTaskId())) {
                dangling.add(dep.toTaskId());
            }
        }
        if (!dangling.isEmpty()) {
            List<String> sorted = new ArrayList<>(dangling);
            Collections.sort(sorted);
            throw new IllegalArgumentException(
                    "dependencies reference unknown tasks: " + sorted);
        }
    }

    /**
     * Validates that the given tasks and dependencies form a Directed Acyclic
     * Graph, throwing {@link PlanValidationException} when a cycle exists.
     *
     * <p>Cycle detection is deterministic: the offending task identifiers are
     * reported in ascending lexicographic order.</p>
     *
     * @param tasks        the tasks to validate (must not be null)
     * @param dependencies the dependencies to validate (must not be null)
     * @throws PlanValidationException if a cycle exists
     */
    public static void validateAcyclic(List<PlanningTask> tasks, List<TaskDependency> dependencies) {
        List<String> cyclic = findCyclicTaskIds(tasks, dependencies);
        if (!cyclic.isEmpty()) {
            PlanningError error = new PlanningError(
                    PlanningErrorCode.VALIDATION_ERROR,
                    "Task graph contains a cycle among tasks: " + cyclic,
                    Instant.now(),
                    Map.of("cyclicTaskIds", cyclic));
            throw new PlanValidationException(error);
        }
    }

    /**
     * Returns the identifiers of the tasks that participate in a cycle, in
     * ascending lexicographic order, or an empty list when the graph is
     * acyclic.
     *
     * <p>Implemented with Kahn's algorithm - iterative, allocation-bounded,
     * and free of recursion.</p>
     *
     * @param tasks        the tasks (null is treated as empty)
     * @param dependencies the dependencies (null is treated as empty)
     * @return the sorted cyclic identifiers, or an empty list when acyclic
     */
    public static List<String> findCyclicTaskIds(List<PlanningTask> tasks, List<TaskDependency> dependencies) {
        List<PlanningTask> safeTasks = tasks == null ? List.of() : tasks;
        List<TaskDependency> safeDeps = dependencies == null ? List.of() : dependencies;
        List<PlanningTask> resolved = topologicalOrder(safeTasks, safeDeps);
        if (resolved.size() == safeTasks.size()) {
            return List.of();
        }
        Set<String> resolvedIds = new HashSet<>();
        for (PlanningTask task : resolved) {
            resolvedIds.add(task.taskId());
        }
        List<String> cyclic = new ArrayList<>();
        for (PlanningTask task : safeTasks) {
            if (!resolvedIds.contains(task.taskId())) {
                cyclic.add(task.taskId());
            }
        }
                Collections.sort(cyclic);
        return List.copyOf(cyclic);
    }

    /**
     * Produces a deterministic topological ordering of the supplied tasks.
     *
     * <p>A predecessor finishes before its successor starts: edge
     * {@code fromTaskId -> toTaskId} increments the successor's in-degree.</p>
     *
     * @param tasks        the tasks (must not be null)
     * @param dependencies the dependencies (must not be null)
     * @return the tasks in a deterministic topological order (never null)
     */
    public static List<PlanningTask> topologicalOrder(List<PlanningTask> tasks, List<TaskDependency> dependencies) {
        Map<String, PlanningTask> byId = new LinkedHashMap<>();
        Map<String, List<String>> successors = new LinkedHashMap<>();
        Map<String, Integer> indegree = new LinkedHashMap<>();
        for (PlanningTask task : tasks) {
            Objects.requireNonNull(task, "task must not be null");
            byId.put(task.taskId(), task);
            successors.put(task.taskId(), new ArrayList<>());
            indegree.put(task.taskId(), 0);
        }
        for (TaskDependency dep : dependencies) {
            Objects.requireNonNull(dep, "dependency must not be null");
            if (byId.containsKey(dep.fromTaskId()) && byId.containsKey(dep.toTaskId())) {
                successors.get(dep.fromTaskId()).add(dep.toTaskId());
                indegree.merge(dep.toTaskId(), 1, Integer::sum);
            }
        }
        PriorityQueue<PlanningTask> ready = new PriorityQueue<>(PlanningTask.canonicalOrder());
        for (PlanningTask task : tasks) {
            if (indegree.get(task.taskId()) == 0) {
                ready.add(task);
            }
        }
        List<PlanningTask> ordered = new ArrayList<>(tasks.size());
        while (!ready.isEmpty()) {
            PlanningTask current = ready.poll();
            ordered.add(current);
            List<String> nexts = successors.get(current.taskId());
            if (nexts != null) {
                for (String nextId : nexts) {
                    int remaining = indegree.merge(nextId, -1, Integer::sum);
                    if (remaining == 0) {
                        ready.add(byId.get(nextId));
                    }
                }
            }
        }
                return List.copyOf(ordered);
    }

    /**
     * Returns this graph's tasks in canonical order: composite order, then
     * title, then identifier. The returned list is a defensive copy.
     *
     * @return the canonical-ordered tasks (never null)
     */
    public List<PlanningTask> tasksInCanonicalOrder() {
        List<PlanningTask> copy = new ArrayList<>(tasks);
        copy.sort(PlanningTask.canonicalOrder());
        return List.copyOf(copy);
    }

    /**
     * Returns this graph's dependencies in canonical order: predecessor
     * order, successor order, dependency type ordinal, then identifier pair.
     *
     * @return the canonical-ordered dependencies (never null)
     */
    public List<TaskDependency> dependenciesInCanonicalOrder() {
        Map<String, Integer> orderById = new LinkedHashMap<>();
        int index = 0;
        for (PlanningTask task : tasks) {
            orderById.put(task.taskId(), index++);
        }
        int fallback = orderById.size();
        List<TaskDependency> copy = new ArrayList<>(dependencies);
        copy.sort(
                Comparator.comparingInt((TaskDependency d) -> orderById.getOrDefault(d.fromTaskId(), fallback))
                        .thenComparingInt(d -> orderById.getOrDefault(d.toTaskId(), fallback))
                        .thenComparing(d -> d.type().ordinal())
                        .thenComparing(TaskDependency::fromTaskId)
                        .thenComparing(TaskDependency::toTaskId));
        return List.copyOf(copy);
    }

    /**
     * Returns a graph whose task list is in deterministic topological order.
     *
     * @return a topologically ordered graph (never null)
     */
    public TaskGraph topologicallyOrdered() {
        List<PlanningTask> ordered = topologicalOrder(tasks, dependencies);
        if (ordered.equals(tasks)) {
            return this;
        }
        return new TaskGraph(ordered, dependencies);
    }

    /**
     * Reports whether this graph contains the given task identifier.
     *
     * @param taskId the identifier to look up (may be null)
     * @return {@code true} when the identifier belongs to a task in this graph
     */
    public boolean containsTask(String taskId) {
        return taskIds().contains(taskId);
    }

    /**
     * Returns the task with the given identifier, or an empty optional when
     * absent.
     *
     * @param taskId the identifier to look up (must not be null)
     * @return the matching task, or an empty optional
     */
    public java.util.Optional<PlanningTask> taskById(String taskId) {
        Objects.requireNonNull(taskId, "taskId must not be null");
        for (PlanningTask task : tasks) {
            if (task.taskId().equals(taskId)) {
                return java.util.Optional.of(task);
            }
        }
        return java.util.Optional.empty();
    }

    /**
     * Returns the task identifiers in declaration order.
     *
     * @return an unmodifiable set of task identifiers (never null)
     */
    public Set<String> taskIds() {
        Set<String> ids = new LinkedHashSet<>();
        for (PlanningTask task : tasks) {
            ids.add(task.taskId());
        }
        return Collections.unmodifiableSet(ids);
    }

    /**
     * Returns the outgoing edges of the given task, in declaration order.
     *
     * @param taskId the source task identifier (must not be null)
     * @return the outgoing edges (never null, possibly empty)
     */
    public List<TaskDependency> outgoing(String taskId) {
        Objects.requireNonNull(taskId, "taskId must not be null");
        List<TaskDependency> result = new ArrayList<>();
        for (TaskDependency dep : dependencies) {
            if (dep.fromTaskId().equals(taskId)) {
                result.add(dep);
            }
        }
        return List.copyOf(result);
    }

    /**
     * Returns the incoming edges of the given task, in declaration order.
     *
     * @param taskId the target task identifier (must not be null)
     * @return the incoming edges (never null, possibly empty)
     */
    public List<TaskDependency> incoming(String taskId) {
        Objects.requireNonNull(taskId, "taskId must not be null");
        List<TaskDependency> result = new ArrayList<>();
        for (TaskDependency dep : dependencies) {
            if (dep.toTaskId().equals(taskId)) {
                result.add(dep);
            }
        }
        return List.copyOf(result);
    }

    /** @return the number of tasks in this graph */
    public int taskCount() {
        return tasks.size();
    }

    /** @return the number of dependencies in this graph */
    public int dependencyCount() {
        return dependencies.size();
    }

    /** @return {@code true} when this graph has no tasks and no dependencies */
    public boolean isEmpty() {
        return tasks.isEmpty() && dependencies.isEmpty();
    }

    @Override
    public String toString() {
        return "TaskGraph{tasks=" + tasks.size()
                + ", dependencies=" + dependencies.size()
                + ", acyclic=true}";
    }
}