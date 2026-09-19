package com.shreeai.os.platform.runtime.graph;

import com.shreeai.os.platform.resolver.CapabilityType;

import java.util.Collections;
import java.util.Map;

/**
 * <b>ExecutionNodeResult</b>
 *
 * <p>Immutable outcome of executing a single graph node.</p>
 *
 * <p>Carries the node identity, terminal status, output payload, confidence,
 * error (when failed/blocked/skipped) and timing information.</p>
 *
 * <p><b>Ownership:</b> Runtime Kernel (Graph Execution)</p>
 */
public final class ExecutionNodeResult {

    /** Terminal status of a node execution. */
    public enum NodeStatus {
        /** Node executed successfully. */
        COMPLETED,
        /** Node executor reported a failure. */
        FAILED,
        /** Node was not executed (dependency failed upstream). */
        SKIPPED,
        /** Node was blocked by a governance interceptor (e.g. Safety). */
        BLOCKED
    }

    private final String nodeId;
    private final CapabilityType capability;
    private final NodeStatus status;
    private final String output;
    private final double confidence;
    private final String message;
    private final long durationMillis;
    private final Map<String, Object> metadata;

    private ExecutionNodeResult(
            String nodeId,
            CapabilityType capability,
            NodeStatus status,
            String output,
            double confidence,
            String message,
            long durationMillis,
            Map<String, Object> metadata) {
        this.nodeId = nodeId;
        this.capability = capability;
        this.status = status;
        this.output = output;
        this.confidence = confidence;
        this.message = message == null ? "" : message;
        this.durationMillis = durationMillis;
        this.metadata = metadata == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(metadata);
    }

    public static ExecutionNodeResult completed(
            String nodeId, CapabilityType capability, String output,
            double confidence, long durationMillis) {
        return new ExecutionNodeResult(nodeId, capability, NodeStatus.COMPLETED,
                output, clamp(confidence), "", durationMillis, null);
    }

    public static ExecutionNodeResult failed(
            String nodeId, CapabilityType capability, String message,
            long durationMillis) {
        return new ExecutionNodeResult(nodeId, capability, NodeStatus.FAILED,
                "", 0.0, message, durationMillis, null);
    }

    public static ExecutionNodeResult skipped(
            String nodeId, CapabilityType capability, String message) {
        return new ExecutionNodeResult(nodeId, capability, NodeStatus.SKIPPED,
                "", 0.0, message, 0L, null);
    }

    public static ExecutionNodeResult blocked(
            String nodeId, CapabilityType capability, String message) {
        return new ExecutionNodeResult(nodeId, capability, NodeStatus.BLOCKED,
                "", 0.0, message, 0L, null);
    }

    /**
     * Returns a copy of this result with the given metadata map replacing any
     * existing metadata.
     *
     * @param metadata the new metadata (may be null)
     * @return a new result with the same fields but the given metadata
     */
    public ExecutionNodeResult withMetadata(Map<String, Object> metadata) {
        return new ExecutionNodeResult(nodeId, capability, status,
                output, confidence, message, durationMillis, metadata);
    }

    public String nodeId() {
        return nodeId;
    }

    public CapabilityType capability() {
        return capability;
    }

    public NodeStatus status() {
        return status;
    }

    public String output() {
        return output;
    }

    public double confidence() {
        return confidence;
    }

    public String message() {
        return message;
    }

    public long durationMillis() {
        return durationMillis;
    }

    public Map<String, Object> metadata() {
        return metadata;
    }

    public boolean isSuccess() {
        return status == NodeStatus.COMPLETED;
    }

    private static double clamp(double confidence) {
        if (confidence < 0.0) {
            return 0.0;
        }
        return Math.min(confidence, 1.0);
    }

    @Override
    public String toString() {
        return "ExecutionNodeResult{nodeId=" + nodeId
                + ", capability=" + capability
                + ", status=" + status
                + ", confidence=" + confidence
                + ", durationMillis=" + durationMillis
                + (message.isEmpty() ? "" : ", message=" + message)
                + "}";
    }
}
