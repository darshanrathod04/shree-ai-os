package com.shreeai.os.platform.runtime.interceptor;

import com.shreeai.os.platform.resolver.CapabilityType;
import com.shreeai.os.platform.security.api.ApprovalService;
import com.shreeai.os.platform.security.api.PermissionManager;
import com.shreeai.os.platform.security.model.ApprovalRequest;
import com.shreeai.os.platform.security.model.PermissionDecision;
import com.shreeai.os.platform.security.model.PermissionRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <b>DefaultSafetyInterceptor</b>
 *
 * <p>The default runtime safety gate. It enforces two layers, evaluated in order:</p>
 * <ol>
 *   <li><b>Destructive pattern deny-list</b> — a small, deterministic set of
 *       case-insensitive patterns for unambiguously destructive operations
 *       (drop table, delete from, rm -rf, …). A match yields a hard
 *       {@link InterceptorResult#deny(String)} and immediately stops graph
 *       execution.</li>
 *   <li><b>Permission check / approval escalation</b> — the request payload is
 *       submitted to the {@link PermissionManager}. When the decision is
 *       {@link PermissionDecision#DENY} an immediate deny is returned. When the
 *       decision is {@link PermissionDecision#ASK_USER} an approval ticket is
 *       registered with the {@link ApprovalService} and an
 *       {@link InterceptorResult#requireApproval(String, String)} is returned
 *       carrying the approval request id.</li>
 * </ol>
 *
 * <p>When no pattern matches and the permission manager has nothing to complain
 * about, {@link InterceptorResult#allow()} is returned and the node proceeds
 * to execution.</p>
 *
 * <p>This gate <b>reuses</b> the canonical {@link PermissionManager} and
 * {@link ApprovalService} rather than re-implementing policy, so all
 * permission / approval state lives in a single place.</p>
 *
 * <p><b>Ownership:</b> Runtime Kernel (Safety Gate)</p>
 */
public final class DefaultSafetyInterceptor implements SafetyInterceptor {

    /**
     * Deterministic deny-list patterns (evaluated case-insensitively as
     * substring matches). Kept intentionally conservative and aligned with the
     * graph-runtime SafetyInterceptor so behaviour never diverges from the
     * legacy green baseline.
     */
    private static final List<String> DENY_PATTERNS = List.of(
            "rm -rf ",
            "drop table",
            "delete from ",
            "shutdown -",
            "format c:",
            "__import__('os').system",
            "drop database"
    );

    /**
     * Capabilities whose default action is treated as a destructive operation.
     */
    private static final Set<String> DESTRUCTIVE_CAPABILITIES =
            Set.of("DROP_TABLE", "DELETE_FROM", "EXEC", "SHUTDOWN");

    private final PermissionManager permissionManager;
    private final ApprovalService approvalService;

    /**
     * Creates a gate backed by the given permission and approval services.
     *
     * @param permissionManager the canonical permission engine, or null for a
     *                          deny-list-only gate
     * @param approvalService   the canonical approval registry, or null to use a
     *                          synthetic approval id on ASK_USER
     */
    public DefaultSafetyInterceptor(PermissionManager permissionManager,
                                    ApprovalService approvalService) {
        // Services may be null: a no-service gate performs the deny-list only.
        // The permission manager is consulted when present; the approval
        // service persists ASK_USER decisions, falling back to a synthetic id
        // when absent so the REQUIRE_APPROVAL path stays observable.
        this.permissionManager = permissionManager;
        this.approvalService = approvalService;
    }

    /**
     * Creates a gate backed by the given permission manager with no approval
     * service wired. A {@link PermissionDecision#ASK_USER} decision surfaces as an
     * {@link InterceptorResult#requireApproval(String, String)} carrying a
     * synthetic approval id (no ticket can be persisted without an approval
     * service).
     *
     * @param permissionManager the canonical permission engine (may be null)
     */
    public DefaultSafetyInterceptor(PermissionManager permissionManager) {
        this(permissionManager, null);
    }

    /**
     * Creates a gate with no permission or approval backing. This performs the
     * deny-list check only — every payload that is not on the deny-list is
     * allowed. Suitable for tests that exercise the gate in isolation.
     */
    public DefaultSafetyInterceptor() {
        this(null, null);
    }

    @Override
    public InterceptorResult before(com.shreeai.os.platform.graph.ExecutionNode node,
                                    PermissionContext context) {
        String payload = context.payload();

        // 1. Deterministic destructive-pattern deny-list (no services required).
        String normalized = payload.toLowerCase();
        for (String pattern : DENY_PATTERNS) {
            if (normalized.contains(pattern)) {
                return InterceptorResult.deny(
                        "safety: destructive pattern detected: " + pattern.trim());
            }
        }

        // 2. When no deny-list match, consult the canonical permission manager.
        if (permissionManager != null) {
            PermissionRequest permissionRequest =
                    buildPermissionRequest(node, context, payload);
            PermissionDecision decision = permissionManager.check(permissionRequest);
            return switch (decision) {
                case DENY -> InterceptorResult.deny(
                        "safety: denied by permission manager");
                case ASK_USER -> {
                    String approvalId = registerApproval(permissionRequest);
                    yield InterceptorResult.requireApproval(
                            "safety: approval required by permission manager",
                            approvalId);
                }
                case ALLOW -> InterceptorResult.allow();
            };
        }

        return InterceptorResult.allow();
    }

    /**
     * Builds the canonical {@link PermissionRequest} describing the node about
     * to execute. The permission manager is free to inspect whatever it needs
     * from the {@code toolId} / {@code operation} / {@code metadata} tuple.
     */
    private PermissionRequest buildPermissionRequest(com.shreeai.os.platform.graph.ExecutionNode node,
                                                     PermissionContext context,
                                                     String payload) {
        String toolId = node.capability() == null
                ? "UNKNOWN" : node.capability().name();
        String operation = resolveAction(node.capability());
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("resource", payload);
        putIfNotNull(metadata, "requestId", context.requestId());
        putIfNotNull(metadata, "sessionId", context.sessionId());
        putIfNotNull(metadata, "requestType", context.requestType());
        return new PermissionRequest(toolId, operation, Map.copyOf(metadata));
    }

    /**
     * Adds the entry only when the value is non-null, so the resulting map can be
     * wrapped in an immutable {@link Map#copyOf} (which rejects null values)
     * without throwing. A missing session simply yields an absent entry rather
     * than a null value.
     */
    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    /**
     * Registers an approval ticket for an {@link PermissionDecision#ASK_USER}
     * decision. When no approval service is wired (e.g. in standalone tests), a
     * synthetic id is derived from the request id so the caller can still
     * observe the REQUIRE_APPROVAL path end-to-end.
     */
    private String registerApproval(PermissionRequest permissionRequest) {
        if (approvalService == null) {
            Object requestId = permissionRequest.metadata().get("requestId");
            return "runtime:" + (requestId == null ? "unknown" : requestId);
        }
        ApprovalRequest approval = approvalService.create(
                ApprovalRequest.pending(
                        permissionRequest.toolId(),
                        permissionRequest.operation(),
                        permissionRequest.metadata()));
        return approval.requestId();
    }

    /**
     * Derives a coarse operation label from the capability type. The permission
     * manager owns the authoritative policy; this is only a hint.
     */
    private static String resolveAction(CapabilityType capability) {
        if (capability == null) {
            return "execute";
        }
        if (DESTRUCTIVE_CAPABILITIES.contains(capability.name())) {
            return "execute:destructive";
        }
        return switch (capability) {
            case IDENTITY, MEMORY, KNOWLEDGE -> "read";
            default -> "execute";
        };
    }
}
