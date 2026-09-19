package com.shreeai.os.platform.graph;

import com.shreeai.os.platform.resolver.CapabilityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ExecutionGraph</b>
 *
 * <p>Immutable Universal Execution Graph produced by {@code ExecutionGraphBuilder}
 * from a {@code CapabilityPlan}.</p>
 *
 * <p>A graph converts a capability plan into executable nodes and the edges
 * that connect them. It is a planning artifact only: the graph model is never
 * executed, and the Runtime ignores it.</p>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final.</li>
 *   <li>Builder-only construction — no public constructors.</li>
 *   <li>Defensive copies — protects mutable collections.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Graph</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class ExecutionGraph {

    private final String graphId;
    private final String requestId;
    private final List<ExecutionNode> nodes;
    private final List<ExecutionEdge> edges;
    private final ExecutionNode rootNode;
    private final Map<String, Object> metadata;

    private ExecutionGraph(Builder builder) {
        this.graphId = Objects.requireNonNull(builder.graphId, "graphId must not be null");
        this.requestId = Objects.requireNonNull(builder.requestId, "requestId must not be null");
        this.nodes = List.copyOf(builder.nodes);
        this.edges = List.copyOf(builder.edges);
        this.rootNode = Objects.requireNonNull(builder.rootNode, "rootNode must not be null");
        this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(builder.metadata));
    }

    /**
     * Returns the unique identifier of this graph.
     *
     * @return the graph ID
     */
    public String graphId() {
        return graphId;
    }

    /**
     * Returns the identifier of the request this graph was built for.
     *
     * @return the request ID
     */
    public String requestId() {
        return requestId;
    }

    /**
     * Returns the nodes of this graph.
     *
     * @return an immutable list of nodes (never null)
     */
    public List<ExecutionNode> nodes() {
        return nodes;
    }

    /**
     * Returns the edges of this graph.
     *
     * @return an immutable list of edges (never null)
     */
    public List<ExecutionEdge> edges() {
        return edges;
    }

    /**
     * Returns the root node of this graph. Per graph rules the root is always
     * the Identity node.
     *
     * @return the root node
     */
    public ExecutionNode rootNode() {
        return rootNode;
    }

    /**
     * Returns metadata describing how this graph was produced.
     *
     * @return an immutable metadata map (never null)
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Returns the node with the given ID, or {@code null} if absent.
     *
     * @param nodeId the node ID
     * @return the node, or null
     */
    public ExecutionNode nodeById(String nodeId) {
        for (ExecutionNode node : nodes) {
            if (node.nodeId().equals(nodeId)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Returns whether this graph contains a node for the given capability.
     *
     * @param capability the capability
     * @return true if a node exists for the capability
     */
    public boolean contains(CapabilityType capability) {
        return nodes.stream().anyMatch(n -> n.capability() == capability);
    }

    /**
     * Returns a new builder for {@link ExecutionGraph}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link ExecutionGraph}.
     */
    public static final class Builder {

        private String graphId;
        private String requestId;
        private final List<ExecutionNode> nodes = new ArrayList<>();
        private final List<ExecutionEdge> edges = new ArrayList<>();
        private ExecutionNode rootNode;
        private final Map<String, Object> metadata = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder graphId(String graphId) {
            this.graphId = graphId;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder nodes(List<ExecutionNode> nodes) {
            this.nodes.addAll(
                    Objects.requireNonNull(nodes, "nodes must not be null"));
            return this;
        }

        public Builder addNode(ExecutionNode node) {
            this.nodes.add(
                    Objects.requireNonNull(node, "node must not be null"));
            return this;
        }

        public Builder edges(List<ExecutionEdge> edges) {
            this.edges.addAll(
                    Objects.requireNonNull(edges, "edges must not be null"));
            return this;
        }

        public Builder addEdge(ExecutionEdge edge) {
            this.edges.add(
                    Objects.requireNonNull(edge, "edge must not be null"));
            return this;
        }

        public Builder rootNode(ExecutionNode rootNode) {
            this.rootNode = rootNode;
            return this;
        }

        public Builder metadata(String key, Object value) {
            this.metadata.put(
                    Objects.requireNonNull(key, "key must not be null"), value);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata.putAll(
                    Objects.requireNonNull(metadata, "metadata must not be null"));
            return this;
        }

        public ExecutionGraph build() {
            return new ExecutionGraph(this);
        }
    }

    @Override
    public String toString() {
        return "ExecutionGraph{"
                + "graphId='" + graphId + '\''
                + ", requestId='" + requestId + '\''
                + ", nodes=" + nodes.size()
                + ", edges=" + edges.size()
                + ", rootNode=" + (rootNode != null ? rootNode.nodeId() : null)
                + '}';
    }
}