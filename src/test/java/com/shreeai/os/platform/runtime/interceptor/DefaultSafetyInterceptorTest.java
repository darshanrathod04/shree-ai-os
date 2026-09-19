package com.shreeai.os.platform.runtime.interceptor;

import com.shreeai.os.platform.graph.ExecutionNode;
import com.shreeai.os.platform.graph.NodeType;
import com.shreeai.os.platform.resolver.CapabilityType;
import com.shreeai.os.platform.runtime.execution.ExecutionRequest;
import com.shreeai.os.platform.security.api.PermissionManager;
import com.shreeai.os.platform.security.model.PermissionDecision;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DefaultSafetyInterceptor}.
 *
 * <p>Covers the two-layer gate in isolation — the deterministic deny-list
 * (layer 1) and the canonical {@link PermissionManager} integration (layer 2)
 * — without spinning up the full graph runtime.</p>
 */
class DefaultSafetyInterceptorTest {

    private static ExecutionNode identityNode() {
        return ExecutionNode.builder()
                .nodeId("IDENTITY")
                .capability(CapabilityType.IDENTITY)
                .nodeType(NodeType.forCapability(CapabilityType.IDENTITY))
                .build();
    }

    private static ExecutionNode knowledgeNode() {
        return ExecutionNode.builder()
                .nodeId("KNOWLEDGE")
                .capability(CapabilityType.KNOWLEDGE)
                .nodeType(NodeType.forCapability(CapabilityType.KNOWLEDGE))
                .build();
    }

    private static ExecutionRequest request(String payload, String requestId) {
        return ExecutionRequest.builder()
                .requestId(requestId)
                .payload(payload)
                .build();
    }

    @Test
    void denyListBlocksDropTable() {
        DefaultSafetyInterceptor gate = new DefaultSafetyInterceptor();
        ExecutionNode node = identityNode();
        PermissionContext context = PermissionContext.of(node,
                request("drop table users", "req-drop"));

        InterceptorResult result = gate.before(node, context);

        assertFalse(result.isAllowed());
        assertEquals(InterceptorResult.Outcome.DENY, result.outcome());
        assertTrue(result.reason().contains("destructive pattern"), result.reason());
    }

    @Test
    void denyListBlocksDeleteFrom() {
        DefaultSafetyInterceptor gate = new DefaultSafetyInterceptor();
        PermissionContext context = PermissionContext.of(identityNode(),
                request("delete from accounts", "req-delete"));

        InterceptorResult result = gate.before(identityNode(), context);

        assertFalse(result.isAllowed());
        assertEquals(InterceptorResult.Outcome.DENY, result.outcome());
    }

    @Test
    void benignPayloadIsAllowed() {
        DefaultSafetyInterceptor gate = new DefaultSafetyInterceptor();
        PermissionContext context = PermissionContext.of(knowledgeNode(),
                request("What is the capital of France?", "req-ok"));

        InterceptorResult result = gate.before(knowledgeNode(), context);

        assertTrue(result.isAllowed());
        assertEquals(InterceptorResult.Outcome.ALLOW, result.outcome());
    }

    @Test
    void denyListTakesPrecedenceOverAllowingPermissionManager() {
        // A permissive permission manager must NOT override the deny-list.
        PermissionManager permissive = req -> PermissionDecision.ALLOW;
        DefaultSafetyInterceptor gate = new DefaultSafetyInterceptor(permissive);
        PermissionContext context = PermissionContext.of(identityNode(),
                request("drop table users; --", "req-precedence"));

        InterceptorResult result = gate.before(identityNode(), context);

        assertFalse(result.isAllowed());
        assertEquals(InterceptorResult.Outcome.DENY, result.outcome());
        assertTrue(result.reason().contains("destructive pattern"), result.reason());
    }

    @Test
    void permissionManagerDenyIsRespected() {
        PermissionManager denying = req -> PermissionDecision.DENY;
        DefaultSafetyInterceptor gate = new DefaultSafetyInterceptor(denying);
        PermissionContext context = PermissionContext.of(identityNode(),
                request("SELECT 1", "req-deny"));

        InterceptorResult result = gate.before(identityNode(), context);

        assertFalse(result.isAllowed());
        assertEquals(InterceptorResult.Outcome.DENY, result.outcome());
        assertTrue(result.reason().contains("permission manager"), result.reason());
    }

    @Test
    void permissionManagerAskUserEscalatesToApproval() {
        PermissionManager asking = req -> PermissionDecision.ASK_USER;
        DefaultSafetyInterceptor gate = new DefaultSafetyInterceptor(asking);
        PermissionContext context = PermissionContext.of(knowledgeNode(),
                request("Run a destructive schema migration", "perm-ask-1"));

        InterceptorResult result = gate.before(knowledgeNode(), context);

        assertFalse(result.isAllowed());
        assertEquals(InterceptorResult.Outcome.REQUIRE_APPROVAL, result.outcome());
        assertTrue(result.requiresApproval());
        assertNotNull(result.approvalRequestId());
        // No ApprovalService is wired, so the gate mints a synthetic correlation id.
        assertEquals("runtime:" + context.requestId(), result.approvalRequestId());
    }

    @Test
    void withNodeIdAttachesNodeIdentity() {
        DefaultSafetyInterceptor gate = new DefaultSafetyInterceptor();
        PermissionContext context = PermissionContext.of(identityNode(),
                request("drop table users", "req-nodeid"));

        InterceptorResult result = gate.before(identityNode(), context)
                .withNodeId("IDENTITY");

        assertEquals("IDENTITY", result.nodeId());
    }
}
