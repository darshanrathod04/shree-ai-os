package com.shreeai.os.platform.runtime.interceptor;

import java.util.Objects;

/**
 * <b>InterceptorResult</b>
 *
 * <p>Terminal outcome of a runtime safety gate evaluation. A gate returns one
 * of three mutually-exclusive states:</p>
 * <ul>
 *   <li>{@link Outcome#ALLOW} — the node may proceed to execution.</li>
 *   <li>{@link Outcome#DENY} — the node is blocked and graph execution stops
 *       after the current node (the node is recorded as {@code BLOCKED}).</li>
 *   <li>{@link Outcome#REQUIRE_APPROVAL} — the node is blocked pending an
 *       explicit human/operator approval; the {@link #approvalRequestId()}
 *       token is the correlation id for the approval ticket.</li>
 * </ul>
 *
 * <p>Instances are created via the static factory methods {@link #allow()},
 * {@link #deny(String)} and {@link #requireApproval(String, String)}. An
 * optional {@code nodeId} identifying the node the decision applies to can be
 * attached with {@link #withNodeId(String)}; it is populated by the graph
 * runtime when it records a non-allow decision so the structured payload can
 * report which node was affected.</p>
 *
 * <p><b>Ownership:</b> Runtime Kernel (Safety Gate)</p>
 */
public final class InterceptorResult {

    /**
     * Terminal outcome of a safety gate evaluation.
     */
    public enum Outcome {
        /**
         * The node is permitted to execute.
         */
        ALLOW,
        /**
         * The node is denied and must not execute.
         */
        DENY,
        /**
         * The node is denied and awaits explicit approval.
         */
        REQUIRE_APPROVAL
    }

    private static final InterceptorResult ALLOW =
            new InterceptorResult(Outcome.ALLOW, "", null, null);

    private final Outcome outcome;
    private final String reason;
    private final String approvalRequestId;
    private final String nodeId;

    private InterceptorResult(Outcome outcome, String reason,
                              String approvalRequestId, String nodeId) {
        this.outcome = Objects.requireNonNull(outcome, "outcome must not be null");
        this.reason = reason == null ? "" : reason;
        this.approvalRequestId = approvalRequestId;
        this.nodeId = nodeId;
    }

    /**
     * @return the canonical ALLOW decision
     */
    public static InterceptorResult allow() {
        return ALLOW;
    }

    /**
     * Builds a DENY decision with the given human-readable reason.
     *
     * @param reason why the node is denied (null → empty string)
     * @return a non-null DENY decision
     */
    public static InterceptorResult deny(String reason) {
        return new InterceptorResult(Outcome.DENY, reason, null, null);
    }

    /**
     * Builds a REQUIRE_APPROVAL decision. The node is blocked until an approval
     * carrying the given request id is granted.
     *
     * @param reason            why approval is required (null → empty string)
     * @param approvalRequestId correlation id for the approval ticket (never null)
     * @return a non-null REQUIRE_APPROVAL decision
     */
    public static InterceptorResult requireApproval(String reason, String approvalRequestId) {
        Objects.requireNonNull(approvalRequestId, "approvalRequestId must not be null");
        return new InterceptorResult(Outcome.REQUIRE_APPROVAL, reason, approvalRequestId, null);
    }

    /**
     * Returns a copy of this decision annotated with the id of the node it was
     * taken against, for structured reporting. The canonical {@link #allow()}
     * singleton is returned unchanged by this method only when {@code nodeId}
     * is null; otherwise a distinct instance is returned.
     *
     * @param nodeId the node id to attach (may be null to clear)
     * @return a decision equal to this one but carrying the supplied node id
     */
    public InterceptorResult withNodeId(String nodeId) {
        if (nodeId == null && this.nodeId == null) {
            return this;
        }
        return new InterceptorResult(outcome, reason, approvalRequestId, nodeId);
    }

    /**
     * @return the terminal outcome (never null)
     */
    public Outcome outcome() {
        return outcome;
    }

    /**
     * Equivalent to {@code outcome() == Outcome.ALLOW}.
     *
     * @return true when the node may proceed
     */
    public boolean isAllowed() {
        return outcome == Outcome.ALLOW;
    }

    /**
     * Equivalent to {@code outcome() == Outcome.DENY}.
     *
     * @return true when the node is hard-denied
     */
    public boolean isDenied() {
        return outcome == Outcome.DENY;
    }

    /**
     * Equivalent to {@code outcome() == Outcome.REQUIRE_APPROVAL}.
     *
     * @return true when the node awaits explicit approval
     */
    public boolean requiresApproval() {
        return outcome == Outcome.REQUIRE_APPROVAL;
    }

    /**
     * @return the reason for the decision (never null; empty when none)
     */
    public String reason() {
        return reason;
    }

    /**
     * @return the approval request id when {@link #outcome()} is
     * {@link Outcome#REQUIRE_APPROVAL}, otherwise null
     */
    public String approvalRequestId() {
        return approvalRequestId;
    }

    /**
     * @return the node id this decision was taken against, or null when unset
     */
    public String nodeId() {
        return nodeId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("InterceptorResult{outcome=").append(outcome);
        if (nodeId != null) {
            sb.append(", nodeId='").append(nodeId).append('\'');
        }
        if (!reason.isEmpty()) {
            sb.append(", reason='").append(reason).append('\'');
        }
        if (approvalRequestId != null) {
            sb.append(", approvalRequestId='").append(approvalRequestId).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
}
