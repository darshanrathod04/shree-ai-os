package com.shreeai.os.platform.graph;

import java.util.Objects;

/**
 * <b>ExecutionEdge</b>
 *
 * <p>Immutable directed edge of a {@link ExecutionGraph}.</p>
 *
 * <p><b>Direction semantics:</b> {@code sourceNodeId → targetNodeId} means
 * the <em>source</em> node <strong>depends on</strong> the <em>target</em>
 * node (the target is a prerequisite of the source).</p>
 *
 * <p><b>Ownership:</b> Platform Graph</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class ExecutionEdge {

    private final String edgeId;
    private final String sourceNodeId;
    private final String targetNodeId;
    private final String reason;

    private ExecutionEdge(Builder builder) {
        this.edgeId = Objects.requireNonNull(builder.edgeId, "edgeId must not be null");
        this.sourceNodeId = Objects.requireNonNull(builder.sourceNodeId, "sourceNodeId must not be null");
        this.targetNodeId = Objects.requireNonNull(builder.targetNodeId, "targetNodeId must not be null");
        this.reason = builder.reason != null ? builder.reason : "";
    }

    /**
     * Returns the unique identifier of this edge.
     *
     * @return the edge ID
     */
    public String edgeId() {
        return edgeId;
    }

    /**
     * Returns the ID of the node that depends on the target.
     *
     * @return the source node ID
     */
    public String sourceNodeId() {
        return sourceNodeId;
    }

    /**
     * Returns the ID of the node that is a prerequisite of the source.
     *
     * @return the target node ID
     */
    public String targetNodeId() {
        return targetNodeId;
    }

    /**
     * Returns the reason this dependency edge was generated.
     *
     * @return the reason (may be empty)
     */
    public String reason() {
        return reason;
    }

    /**
     * Returns a new builder for {@link ExecutionEdge}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link ExecutionEdge}.
     */
    public static final class Builder {

        private String edgeId;
        private String sourceNodeId;
        private String targetNodeId;
        private String reason;

        private Builder() {
        }

        public Builder edgeId(String edgeId) {
            this.edgeId = edgeId;
            return this;
        }

        public Builder sourceNodeId(String sourceNodeId) {
            this.sourceNodeId = sourceNodeId;
            return this;
        }

        public Builder targetNodeId(String targetNodeId) {
            this.targetNodeId = targetNodeId;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public ExecutionEdge build() {
            return new ExecutionEdge(this);
        }
    }

    @Override
    public String toString() {
        return "ExecutionEdge{"
                + "edgeId='" + edgeId + '\''
                + ", sourceNodeId='" + sourceNodeId + '\''
                + ", targetNodeId='" + targetNodeId + '\''
                + ", reason='" + reason + '\''
                + '}';
    }
}