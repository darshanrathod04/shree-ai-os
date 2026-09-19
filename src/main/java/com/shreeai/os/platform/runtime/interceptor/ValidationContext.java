package com.shreeai.os.platform.runtime.interceptor;

import com.shreeai.os.platform.graph.ExecutionNode;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.runtime.graph.ExecutionNodeResult;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * <b>ValidationContext</b>
 *
 * <p>Immutable snapshot of the data available to the
 * {@link ValidationInterceptor} after a node execution.</p>
 *
 * <p>Carries the executed node, the raw node result, the originating request,
 * the shared execution context (upstream node outputs), and the attempt number
 * (1 for the first execution, 2+ for retries). It is a pure data carrier with
 * no validation logic.</p>
 *
 * <p><b>Ownership:</b> Runtime Kernel — Validation Interceptor</p>
 */
public final class ValidationContext {

    private final ExecutionNode node;
    private final ExecutionNodeResult result;
    private final ExecutionRequest request;
    private final Map<String, Object> executionContext;
    private final int attempt;

    private ValidationContext(
            ExecutionNode node,
            ExecutionNodeResult result,
            ExecutionRequest request,
            Map<String, Object> executionContext,
            int attempt) {
        this.node = Objects.requireNonNull(node, "ValidationContext node must not be null");
        this.result = Objects.requireNonNull(result, "ValidationContext result must not be null");
        this.request = request;
        this.executionContext = executionContext != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(executionContext))
                : Collections.emptyMap();
        this.attempt = Math.max(attempt, 1);
    }

    /**
     * Creates a {@code ValidationContext} for the given node execution outcome.
     *
     * @param node            the executed node (never null)
     * @param result          the raw node result (never null)
     * @param request         the originating request (may be null)
     * @param executionContext shared execution context carrying upstream outputs
     *                        (may be null)
     * @param attempt         the attempt number (1-based; values &lt; 1 are
     *                        treated as 1)
     * @return an immutable context (never null)
     */
    public static ValidationContext of(
            ExecutionNode node,
            ExecutionNodeResult result,
            ExecutionRequest request,
            Map<String, Object> executionContext,
            int attempt) {
        return new ValidationContext(node, result, request, executionContext, attempt);
    }

    /**
     * Returns the node that was executed.
     *
     * @return the node (never null)
     */
    public ExecutionNode node() {
        return node;
    }

    /**
     * Returns the raw result produced by the node executor.
     *
     * @return the result (never null)
     */
    public ExecutionNodeResult result() {
        return result;
    }

    /**
     * Returns the originating request, when available.
     *
     * @return an optional request
     */
    public Optional<ExecutionRequest> request() {
        return Optional.ofNullable(request);
    }

    /**
     * Returns an unmodifiable view of the shared execution context carrying
     * upstream node outputs keyed by {@code "node:<capability>"}.
     *
     * @return the execution context (never null, may be empty)
     */
    public Map<String, Object> executionContext() {
        return executionContext;
    }

    /**
     * Returns the attempt number (1 for first execution, 2+ for retries).
     *
     * @return the attempt number (always &ge; 1)
     */
    public int attempt() {
        return attempt;
    }

    @Override
    public String toString() {
        return "ValidationContext{node=" + node.nodeId()
                + ", status=" + result.status()
                + ", attempt=" + attempt + '}';
    }
}
