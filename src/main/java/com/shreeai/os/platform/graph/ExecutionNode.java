package com.shreeai.os.platform.graph;

import com.shreeai.os.platform.resolver.CapabilityRequirement;
import com.shreeai.os.platform.resolver.CapabilityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <b>ExecutionNode</b>
 *
 * <p>Immutable node of a {@link ExecutionGraph}. Each node represents a single
 * capability required by a request.</p>
 *
 * <p>The node declares its dependencies by node ID, its priority (inherited
 * from the {@link CapabilityRequirement}), and its current state. The only
 * valid state is {@link State#PENDING}: the graph is a planning artifact and
 * is never executed.</p>
 *
 * <p><b>Ownership:</b> Platform Graph</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class ExecutionNode {

    /**
     * Execution state of a graph node. Nodes are never executed by the graph
     * model, so {@link State#PENDING} is the only state that can exist.
     */
    public enum State {
        /** The node has not been executed. */
        PENDING
    }

    private final String nodeId;
    private final CapabilityType capability;
    private final NodeType nodeType;
    private final List<String> dependencies;
    private final CapabilityRequirement.Priority priority;
    private final State state;

    private ExecutionNode(Builder builder) {
        this.nodeId = Objects.requireNonNull(builder.nodeId, "nodeId must not be null");
        this.capability = Objects.requireNonNull(builder.capability, "capability must not be null");
        this.nodeType = Objects.requireNonNull(builder.nodeType, "nodeType must not be null");
        this.dependencies = Collections.unmodifiableList(
                new ArrayList<>(builder.dependencies));
        this.priority = Objects.requireNonNull(builder.priority, "priority must not be null");
        this.state = builder.state != null ? builder.state : State.PENDING;
    }

    /**
     * Returns the unique identifier of this node within its graph.
     *
     * @return the node ID
     */
    public String nodeId() {
        return nodeId;
    }

    /**
     * Returns the capability represented by this node.
     *
     * @return the capability
     */
    public CapabilityType capability() {
        return capability;
    }

    /**
     * Returns the role this node plays in the graph.
     *
     * @return the node type
     */
    public NodeType nodeType() {
        return nodeType;
    }

    /**
     * Returns the IDs of the nodes this node depends on.
     *
     * @return an immutable list of dependency node IDs (never null)
     */
    public List<String> dependencies() {
        return dependencies;
    }

    /**
     * Returns the priority inherited from the capability requirement.
     *
     * @return the priority
     */
    public CapabilityRequirement.Priority priority() {
        return priority;
    }

    /**
     * Returns the execution state of this node.
     *
     * @return the state (always {@link State#PENDING})
     */
    public State state() {
        return state;
    }

    /**
     * Returns a new builder for {@link ExecutionNode}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link ExecutionNode}.
     */
    public static final class Builder {

        private String nodeId;
        private CapabilityType capability;
        private NodeType nodeType;
        private final List<String> dependencies = new ArrayList<>();
        private CapabilityRequirement.Priority priority = CapabilityRequirement.Priority.REQUIRED;
        private State state = State.PENDING;

        private Builder() {
        }

        public Builder nodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder capability(CapabilityType capability) {
            this.capability = capability;
            return this;
        }

        public Builder nodeType(NodeType nodeType) {
            this.nodeType = nodeType;
            return this;
        }

        public Builder addDependency(String dependencyNodeId) {
            this.dependencies.add(
                    Objects.requireNonNull(dependencyNodeId, "dependencyNodeId must not be null"));
            return this;
        }

        public Builder dependencies(List<String> dependencies) {
            this.dependencies.addAll(
                    Objects.requireNonNull(dependencies, "dependencies must not be null"));
            return this;
        }

        public Builder priority(CapabilityRequirement.Priority priority) {
            this.priority = priority;
            return this;
        }

        public Builder state(State state) {
            this.state = state;
            return this;
        }

        public ExecutionNode build() {
            return new ExecutionNode(this);
        }
    }

    @Override
    public String toString() {
        return "ExecutionNode{"
                + "nodeId='" + nodeId + '\''
                + ", capability=" + capability
                + ", nodeType=" + nodeType
                + ", dependencies=" + dependencies
                + ", priority=" + priority
                + ", state=" + state
                + '}';
    }
}