package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.planning.error.PlanValidationException;
import com.shreeai.os.platform.kernels.planning.error.PlanningErrorCode;
import com.shreeai.os.platform.kernels.planning.model.ExecutionEdge;
import com.shreeai.os.platform.kernels.planning.model.ExecutionNode;
import com.shreeai.os.platform.kernels.planning.model.ExecutionState;
import com.shreeai.os.platform.kernels.planning.model.ExecutablePlanningGraph;
import com.shreeai.os.platform.kernels.planning.model.PlanningTask;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("P2.5: Executable planning graph model")
class ExecutablePlanningGraphModelTest {

    private static String id(String key) {
        return PlanningTask.sha256Hex(key);
    }

    private static ExecutionNode node(String key, int day) {
        return new ExecutionNode(id(key), "task-" + key, "Title " + key, day, ExecutionState.READY);
    }

    @Nested
    @DisplayName("ExecutionState")
    class ExecutionStateTests {
        @Test @DisplayName("exposes exactly the four locked states")
        void fourStates() {
            assertEquals(4, ExecutionState.values().length);
            assertEquals("PENDING", ExecutionState.valueOf("PENDING").name());
            assertEquals("READY", ExecutionState.valueOf("READY").name());
            assertEquals("BLOCKED", ExecutionState.valueOf("BLOCKED").name());
            assertEquals("COMPLETED", ExecutionState.valueOf("COMPLETED").name());
        }
        @Test @DisplayName("states are distinct enums")
        void distinct() {
            assertNotEquals(ExecutionState.READY, ExecutionState.BLOCKED);
            assertNotEquals(ExecutionState.COMPLETED, ExecutionState.PENDING);
        }
    }

    @Nested
    @DisplayName("ExecutionNode validation")
    class NodeValidation {
        @Test @DisplayName("a valid node keeps its components")
        void valid() {
            ExecutionNode n = new ExecutionNode(id("a"), "task-a", "Read chapter", 3, ExecutionState.BLOCKED);
            assertEquals(id("a"), n.nodeId());
            assertEquals("task-a", n.taskId());
            assertEquals("Read chapter", n.title());
            assertEquals(3, n.day());
            assertEquals(ExecutionState.BLOCKED, n.state());
        }
        @Test @DisplayName("trims surrounding whitespace of taskId and title")
        void trims() {
            ExecutionNode n = new ExecutionNode(id("a"), "  task-a  ", " Title ", 1, ExecutionState.READY);
            assertEquals("task-a", n.taskId());
            assertEquals("Title", n.title());
        }
        @Test @DisplayName("rejects null components")
        void rejectsNulls() {
            assertThrows(NullPointerException.class, () -> new ExecutionNode(null, "t", "T", 1, ExecutionState.READY));
            assertThrows(NullPointerException.class, () -> new ExecutionNode(id("a"), null, "T", 1, ExecutionState.READY));
            assertThrows(NullPointerException.class, () -> new ExecutionNode(id("a"), "t", null, 1, ExecutionState.READY));
            assertThrows(NullPointerException.class, () -> new ExecutionNode(id("a"), "t", "T", 1, null));
        }
        @Test @DisplayName("rejects blank taskId and title")
        void rejectsBlank() {
            assertThrows(IllegalArgumentException.class, () -> new ExecutionNode(id("a"), "   ", "T", 1, ExecutionState.READY));
            assertThrows(IllegalArgumentException.class, () -> new ExecutionNode(id("a"), "t", "   ", 1, ExecutionState.READY));
        }
        @Test @DisplayName("rejects day below one")
        void rejectsDayZero() {
            assertThrows(IllegalArgumentException.class, () -> new ExecutionNode(id("a"), "t", "T", 0, ExecutionState.READY));
        }
        @Test @DisplayName("accepts day one")
        void acceptsDayOne() {
            assertEquals(1, new ExecutionNode(id("a"), "t", "T", 1, ExecutionState.READY).day());
        }
        @Test @DisplayName("rejects non-SHA-256 node identifiers")
        void rejectsBadNodeIds() {
            assertThrows(IllegalArgumentException.class, () -> new ExecutionNode("abc", "t", "T", 1, ExecutionState.READY));
            assertThrows(IllegalArgumentException.class, () -> new ExecutionNode(id("a").toUpperCase(), "t", "T", 1, ExecutionState.READY));
            assertThrows(IllegalArgumentException.class, () -> new ExecutionNode(id("a").substring(1) + "g", "t", "T", 1, ExecutionState.READY));
            assertThrows(IllegalArgumentException.class, () -> new ExecutionNode("", "t", "T", 1, ExecutionState.READY));
        }
        @Test @DisplayName("isValidNodeId mirrors the SHA-256 hex rule")
        void isValidNodeId() {
            assertTrue(ExecutionNode.isValidNodeId(id("a")));
            assertFalse(ExecutionNode.isValidNodeId("ABC"));
            assertFalse(ExecutionNode.isValidNodeId(null));
            assertFalse(ExecutionNode.isValidNodeId(id("a") + "x"));
        }
        @Test @DisplayName("equality and hash code follow record semantics")
        void equality() {
            ExecutionNode a = new ExecutionNode(id("a"), "t", "T", 2, ExecutionState.READY);
            ExecutionNode b = new ExecutionNode(id("a"), "t", "T", 2, ExecutionState.READY);
            ExecutionNode c = new ExecutionNode(id("a"), "t", "T", 2, ExecutionState.BLOCKED);
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
            assertNotEquals(a, c);
        }
        @Test @DisplayName("toString carries the identifying fields")
        void toStringShapes() {
            String s = new ExecutionNode(id("a"), "task-a", "Read", 1, ExecutionState.READY).toString();
            assertTrue(s.contains("day=1"));
            assertTrue(s.contains("state=READY"));
            assertTrue(s.contains("title='Read'"));
        }
    }

    @Nested
    @DisplayName("ExecutionEdge validation")
    class EdgeValidation {
        @Test @DisplayName("a valid edge keeps its endpoints")
        void valid() {
            ExecutionEdge e = new ExecutionEdge(id("a"), id("b"));
            assertEquals(id("a"), e.fromNodeId());
            assertEquals(id("b"), e.toNodeId());
        }
        @Test @DisplayName("rejects null endpoints")
        void rejectsNull() {
            assertThrows(NullPointerException.class, () -> new ExecutionEdge(null, id("b")));
            assertThrows(NullPointerException.class, () -> new ExecutionEdge(id("a"), null));
        }
        @Test @DisplayName("rejects non-SHA-256 endpoints")
        void rejectsBadIds() {
            assertThrows(IllegalArgumentException.class, () -> new ExecutionEdge("x", id("b")));
            assertThrows(IllegalArgumentException.class, () -> new ExecutionEdge(id("a"), "y"));
        }
        @Test @DisplayName("rejects self edges")
        void rejectsSelfEdge() {
            assertThrows(IllegalArgumentException.class, () -> new ExecutionEdge(id("a"), id("a")));
        }
        @Test @DisplayName("edgeKey is the stable arrow form")
        void edgeKey() {
            assertEquals(id("a") + " -> " + id("b"), new ExecutionEdge(id("a"), id("b")).edgeKey());
        }
        @Test @DisplayName("equality and hash code follow record semantics")
        void equality() {
            assertEquals(new ExecutionEdge(id("a"), id("b")), new ExecutionEdge(id("a"), id("b")));
            assertNotEquals(new ExecutionEdge(id("a"), id("b")), new ExecutionEdge(id("b"), id("a")));
            assertEquals(new ExecutionEdge(id("a"), id("b")).hashCode(), new ExecutionEdge(id("a"), id("b")).hashCode());
        }
    }

    @Nested
    @DisplayName("ExecutablePlanningGraph construction")
    class GraphConstruction {
        @Test @DisplayName("empty() has no nodes and no edges")
        void empty() {
            ExecutablePlanningGraph g = ExecutablePlanningGraph.empty();
            assertTrue(g.nodes().isEmpty());
            assertTrue(g.edges().isEmpty());
        }
        @Test @DisplayName("of() tolerates null lists")
        void ofNull() {
            ExecutablePlanningGraph g = ExecutablePlanningGraph.of(null, null);
            assertTrue(g.nodes().isEmpty());
            assertTrue(g.edges().isEmpty());
        }
        @Test @DisplayName("of() builds an acyclic graph")
        void ofAcyclic() {
            ExecutablePlanningGraph g = ExecutablePlanningGraph.of(
                    List.of(node("a", 1), node("b", 2)),
                    List.of(new ExecutionEdge(id("a"), id("b"))));
            assertEquals(2, g.nodes().size());
            assertEquals(1, g.edges().size());
        }
        @Test @DisplayName("duplicate nodeIds are rejected")
        void duplicateNodeIds() {
            List<ExecutionNode> dup = List.of(node("a", 1), node("a", 1));
            assertThrows(IllegalArgumentException.class,
                    () -> ExecutablePlanningGraph.of(dup, List.of()));
        }
        @Test @DisplayName("edges referencing unknown nodes are rejected")
        void danglingEdge() {
            assertThrows(IllegalArgumentException.class,
                    () -> ExecutablePlanningGraph.of(
                            List.of(node("a", 1)),
                            List.of(new ExecutionEdge(id("a"), id("ghost")))));
            assertThrows(IllegalArgumentException.class,
                    () -> ExecutablePlanningGraph.of(
                            List.of(node("a", 1)),
                            List.of(new ExecutionEdge(id("ghost"), id("a")))));
        }
        @Test @DisplayName("duplicate edges are rejected")
        void duplicateEdge() {
            assertThrows(IllegalArgumentException.class,
                    () -> ExecutablePlanningGraph.of(
                            List.of(node("a", 1), node("b", 2)),
                            List.of(new ExecutionEdge(id("a"), id("b")),
                                    new ExecutionEdge(id("a"), id("b")))));
        }
        @Test @DisplayName("a two-node cycle raises PlanValidationException with VALIDATION_ERROR")
        void twoCycle() {
            PlanValidationException ex = assertThrows(PlanValidationException.class,
                    () -> ExecutablePlanningGraph.of(
                            List.of(node("a", 1), node("b", 2)),
                            List.of(new ExecutionEdge(id("a"), id("b")),
                                    new ExecutionEdge(id("b"), id("a")))));
            assertEquals(PlanningErrorCode.VALIDATION_ERROR, ex.error().code());
        }
        @Test @DisplayName("a three-node cycle is rejected")
        void threeCycle() {
            assertThrows(PlanValidationException.class,
                    () -> ExecutablePlanningGraph.of(
                            List.of(node("a", 1), node("b", 2), node("c", 3)),
                            List.of(new ExecutionEdge(id("a"), id("b")),
                                    new ExecutionEdge(id("b"), id("c")),
                                    new ExecutionEdge(id("c"), id("a")))));
        }
        @Test @DisplayName("a self-loop cannot be formed because edges reject self edges")
        void selfLoopImpossible() {
            assertThrows(IllegalArgumentException.class, () -> new ExecutionEdge(id("a"), id("a")));
        }
        @Test @DisplayName("disconnected components are accepted")
        void disconnectedAccepted() {
            ExecutablePlanningGraph g = ExecutablePlanningGraph.of(
                    List.of(node("a", 1), node("b", 2), node("c", 3), node("d", 4)),
                    List.of(new ExecutionEdge(id("a"), id("b")),
                            new ExecutionEdge(id("c"), id("d"))));
            assertEquals(4, g.nodes().size());
        }
        @Test @DisplayName("a long acyclic chain is accepted without recursion limits")
        void deepChain() {
            int depth = 2000;
            List<ExecutionNode> nodes = new ArrayList<>(depth);
            List<ExecutionEdge> edges = new ArrayList<>(depth - 1);
            for (int i = 0; i < depth; i++) {
                nodes.add(node("n" + i, i + 1));
                if (i > 0) {
                    edges.add(new ExecutionEdge(id("n" + (i - 1)), id("n" + i)));
                }
            }
            ExecutablePlanningGraph g = ExecutablePlanningGraph.of(nodes, edges);
            assertEquals(depth, g.nodes().size());
        }
        @Test @DisplayName("input lists are frozen: later mutation does not leak in")
        void frozenLists() {
            List<ExecutionNode> nodes = new ArrayList<>();
            List<ExecutionEdge> edges = new ArrayList<>();
            nodes.add(node("a", 1));
            ExecutablePlanningGraph g = ExecutablePlanningGraph.of(nodes, edges);
            nodes.add(node("b", 2));
            edges.add(new ExecutionEdge(id("a"), id("b")));
            assertEquals(1, g.nodes().size());
            assertTrue(g.edges().isEmpty());
            assertThrows(UnsupportedOperationException.class, () -> g.nodes().add(node("c", 3)));
        }
    }

    @Nested
    @DisplayName("ExecutablePlanningGraph lookups and equality")
    class GraphLookups {
        private final ExecutablePlanningGraph graph = ExecutablePlanningGraph.of(
                List.of(node("a", 1), node("b", 2)),
                List.of(new ExecutionEdge(id("a"), id("b"))));

        @Test @DisplayName("nodeById finds the exact node")
        void nodeById() {
            assertEquals("task-a", graph.nodeById(id("a")).taskId());
        }
        @Test @DisplayName("nodeById throws for unknown identifiers")
        void nodeByIdUnknown() {
            assertThrows(IllegalArgumentException.class, () -> graph.nodeById(id("ghost")));
        }
        @Test @DisplayName("nodeByTaskId finds the exact node")
        void nodeByTaskId() {
            assertEquals(id("b"), graph.nodeByTaskId("task-b").nodeId());
        }
        @Test @DisplayName("nodeByTaskId throws for unknown task identifiers")
        void nodeByTaskIdUnknown() {
            assertThrows(IllegalArgumentException.class, () -> graph.nodeByTaskId("task-ghost"));
        }
        @Test @DisplayName("graphs with equal content are equal")
        void equality() {
            ExecutablePlanningGraph other = ExecutablePlanningGraph.of(
                    List.of(node("a", 1), node("b", 2)),
                    List.of(new ExecutionEdge(id("a"), id("b"))));
            assertEquals(graph, other);
            assertEquals(graph.hashCode(), other.hashCode());
            assertNotEquals(graph, ExecutablePlanningGraph.empty());
        }
        @Test @DisplayName("toString is non-trivial")
        void toStringShape() {
            assertTrue(graph.toString().contains("ExecutionPlanningGraph")
                    || graph.toString().contains("ExecutablePlanningGraph"));
        }
    }

    @Nested
    @DisplayName("Edge cases and diagnostics")
    class EdgeCasesAndDiagnostics {
        private static String id(String key) {
            return PlanningTask.sha256Hex(key);
        }

        @Test @DisplayName("every execution state constructs a valid node")
        void allStatesConstruct() {
            for (ExecutionState state : ExecutionState.values()) {
                assertEquals(state, new ExecutionNode(id("a"), "t", "T", 1, state).state());
            }
        }

        @Test @DisplayName("a node on a large day value is accepted")
        void largeDay() {
            assertEquals(999, new ExecutionNode(id("a"), "t", "T", 999, ExecutionState.READY).day());
        }

        @Test @DisplayName("edge toString truncates endpoints for diagnostics")
        void edgeToString() {
            String s = new ExecutionEdge(id("a"), id("b")).toString();
            assertTrue(s.contains("ExecutionEdge"));
            assertTrue(s.length() < 80);
        }

        @Test @DisplayName("graph equality fails when any node state differs")
        void equalitySensitiveToState() {
            var ready = ExecutablePlanningGraph.of(
                    List.of(new ExecutionNode(id("a"), "t", "T", 1, ExecutionState.READY)), List.of());
            var blocked = ExecutablePlanningGraph.of(
                    List.of(new ExecutionNode(id("a"), "t", "T", 1, ExecutionState.BLOCKED)), List.of());
            assertNotEquals(ready, blocked);
        }

        @Test @DisplayName("graph equality fails when a single edge differs")
        void equalitySensitiveToEdges() {
            var nodes = List.of(node("a", 1), node("b", 2), node("c", 3));
            var withAb = ExecutablePlanningGraph.of(nodes, List.of(new ExecutionEdge(id("a"), id("b"))));
            var withAc = ExecutablePlanningGraph.of(nodes, List.of(new ExecutionEdge(id("a"), id("c"))));
            assertNotEquals(withAb, withAc);
        }

        @Test @DisplayName("hash codes differ across distinct graphs in practice")
        void hashSpread() {
            var a = ExecutablePlanningGraph.of(List.of(node("a", 1)), List.of());
            var b = ExecutablePlanningGraph.of(List.of(node("b", 1)), List.of());
            assertNotEquals(a.hashCode(), b.hashCode());
        }

        @Test @DisplayName("a diamond DAG is structurally valid")
        void diamondDag() {
            var g = ExecutablePlanningGraph.of(
                    List.of(node("a", 1), node("b", 2), node("c", 2), node("d", 3)),
                    List.of(new ExecutionEdge(id("a"), id("b")),
                            new ExecutionEdge(id("a"), id("c")),
                            new ExecutionEdge(id("b"), id("d")),
                            new ExecutionEdge(id("c"), id("d"))));
            assertEquals(4, g.nodes().size());
            assertEquals(4, g.edges().size());
        }

        @Test @DisplayName("a four-node cycle reports all offenders")
        void fourCycle() {
            var ex = assertThrows(PlanValidationException.class,
                    () -> ExecutablePlanningGraph.of(
                            List.of(node("a", 1), node("b", 2), node("c", 3), node("d", 4)),
                            List.of(new ExecutionEdge(id("a"), id("b")),
                                    new ExecutionEdge(id("b"), id("c")),
                                    new ExecutionEdge(id("c"), id("d")),
                                    new ExecutionEdge(id("d"), id("a")))));
            assertEquals(PlanningErrorCode.VALIDATION_ERROR, ex.error().code());
        }

        @Test @DisplayName("edges may be declared before their nodes without issue")
        void edgesBeforeNodes() {
            var g = ExecutablePlanningGraph.of(
                    List.of(node("b", 2), node("a", 1)),
                    List.of(new ExecutionEdge(id("a"), id("b"))));
            assertEquals(2, g.nodes().size());
            assertEquals(1, g.edges().size());
        }

        @Test @DisplayName("empty graph is never equal to null")
        void neverEqualsNull() {
            assertNotEquals(ExecutablePlanningGraph.empty(), null);
            assertNotEquals(null, ExecutablePlanningGraph.empty());
        }

        @Test @DisplayName("nodeId validation rejects a digest with an uppercase prefix")
        void uppercasePrefixRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ExecutionNode("A" + id("a").substring(1), "t", "T", 1, ExecutionState.READY));
        }

        @Test @DisplayName("node validation rejects a digest with a non-hex suffix")
        void nonHexSuffixRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ExecutionNode(id("a").substring(0, 63) + "z", "t", "T", 1, ExecutionState.READY));
        }
    }
}
