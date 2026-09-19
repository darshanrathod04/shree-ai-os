package com.shreeai.os.platform.runtime.interceptor;

import com.shreeai.os.platform.graph.ExecutionNode;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;

import java.util.Objects;

/**
 * <b>PermissionContext</b>
 *
 * <p>Immutable bundle describing the runtime context in which a safety gate
 * decision is taken. It pairs the graph node about to execute with the
 * originating {@link ExecutionRequest}, giving a gate implementation everything
 * it needs to evaluate a permission without reaching back into the executor.</p>
 *
 * <p>All accessors are total (never null) so gate implementations can safely
 * dereference {@link #payload()} even when the underlying request carried no
 * explicit payload — in that case an empty string is returned.</p>
 *
 * <p><b>Ownership:</b> Runtime Kernel (Safety Gate)</p>
 */
public final class PermissionContext {

    private final ExecutionNode node;
    private final ExecutionRequest request;
    private final String sessionId;

    private PermissionContext(ExecutionNode node, ExecutionRequest request, String sessionId) {
        this.node = Objects.requireNonNull(node, "node must not be null");
        this.request = Objects.requireNonNull(request, "request must not be null");
        this.sessionId = sessionId;
    }

    /**
     * Builds a permission context for the given node and request.
     *
     * @param node    the node about to execute (never null)
     * @param request the originating execution request (never null)
     * @return a non-null permission context
     */
    public static PermissionContext of(ExecutionNode node, ExecutionRequest request) {
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(request, "request must not be null");
        return new PermissionContext(node, request, request.getSession());
    }

    /**
     * The graph node for which a decision is being made.
     *
     * @return the node (never null)
     */
    public ExecutionNode node() {
        return node;
    }

    /**
     * The originating execution request.
     *
     * @return the request (never null)
     */
    public ExecutionRequest request() {
        return request;
    }

    /**
     * The request ID of the originating request.
     *
     * @return the request ID (never null)
     */
    public String requestId() {
        return request.requestId();
    }

    /**
     * The session ID associated with the request, if any.
     *
     * @return the session ID, or null when the request carries no session
     */
    public String sessionId() {
        return sessionId;
    }

    /**
     * Convenience accessor equivalent to {@link ExecutionRequest#payload()}.
     *
     * @return the request payload, or an empty string when none was set
     */
    public String payload() {
        String payload = request.payload();
        return payload == null ? "" : payload;
    }

    /**
     * Convenience accessor equivalent to {@link ExecutionRequest#requestType()}.
     *
     * @return the request type, or an empty string when none was set
     */
    public String requestType() {
        String type = request.requestType();
        return type == null ? "" : type;
    }

    /**
     * The capability type of the node being evaluated.
     *
     * @return the node capability (never null)
     */
    public com.shreeai.os.platform.resolver.CapabilityType capability() {
        return node.capability();
    }
}
