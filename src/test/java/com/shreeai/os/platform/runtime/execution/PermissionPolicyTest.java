package com.shreeai.os.platform.runtime.execution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DefaultPermissionPolicy}.
 */
@DisplayName("DefaultPermissionPolicy Tests")
class PermissionPolicyTest {

    private DefaultPermissionPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new DefaultPermissionPolicy();
    }

    @Test
    @DisplayName("Default decision is ALLOW for unmapped capabilities")
    void defaultDecisionIsAllow() {
        assertEquals(PermissionDecision.ALLOW, policy.evaluate(ExecutionCapability.KNOWLEDGE_SEARCH));
        assertEquals(PermissionDecision.ALLOW, policy.evaluate(ExecutionCapability.TASK_EXECUTION));
    }

    @Test
    @DisplayName("Explicit DENY decision is returned")
    void explicitDenyDecision() {
        policy.set(ExecutionCapability.TASK_EXECUTION, PermissionDecision.DENY);
        assertEquals(PermissionDecision.DENY, policy.evaluate(ExecutionCapability.TASK_EXECUTION));
    }

    @Test
    @DisplayName("Explicit REQUIRE_APPROVAL decision is returned")
    void explicitRequireApprovalDecision() {
        policy.set(ExecutionCapability.PROJECT_PLANNING, PermissionDecision.REQUIRE_APPROVAL);
        assertEquals(PermissionDecision.REQUIRE_APPROVAL, policy.evaluate(ExecutionCapability.PROJECT_PLANNING));
    }

    @Test
    @DisplayName("Custom default decision is applied to unmapped capabilities")
    void customDefaultDecision() {
        DefaultPermissionPolicy customPolicy = new DefaultPermissionPolicy(PermissionDecision.REQUIRE_APPROVAL);
        assertEquals(PermissionDecision.REQUIRE_APPROVAL, customPolicy.evaluate(ExecutionCapability.KNOWLEDGE_SEARCH));
    }

    @Test
    @DisplayName("Clear removes explicit mapping, default applies")
    void clearRemovesMapping() {
        policy.set(ExecutionCapability.MEMORY_RECALL, PermissionDecision.DENY);
        assertEquals(PermissionDecision.DENY, policy.evaluate(ExecutionCapability.MEMORY_RECALL));

        policy.clear(ExecutionCapability.MEMORY_RECALL);
        assertEquals(PermissionDecision.ALLOW, policy.evaluate(ExecutionCapability.MEMORY_RECALL));
    }

    @Test
    @DisplayName("Evaluate null capability throws NullPointerException")
    void evaluateNullThrows() {
        assertThrows(NullPointerException.class, () -> policy.evaluate(null));
    }

    @Test
    @DisplayName("Set null capability throws NullPointerException")
    void setNullCapabilityThrows() {
        assertThrows(NullPointerException.class,
                () -> policy.set(null, PermissionDecision.ALLOW));
    }

    @Test
    @DisplayName("Set null decision throws NullPointerException")
    void setNullDecisionThrows() {
        assertThrows(NullPointerException.class,
                () -> policy.set(ExecutionCapability.KNOWLEDGE_SEARCH, null));
    }

    @Test
    @DisplayName("Clear null capability throws NullPointerException")
    void clearNullThrows() {
        assertThrows(NullPointerException.class, () -> policy.clear(null));
    }

    @Test
    @DisplayName("Policies map is unmodifiable")
    void policiesMapIsUnmodifiable() {
        policy.set(ExecutionCapability.KNOWLEDGE_SEARCH, PermissionDecision.ALLOW);
        assertThrows(UnsupportedOperationException.class,
                () -> policy.policies().put(ExecutionCapability.TASK_EXECUTION, PermissionDecision.DENY));
    }

    @Test
    @DisplayName("DENY is terminal decision")
    void denyIsTerminal() {
        assertTrue(PermissionDecision.DENY.isTerminal());
    }

    @Test
    @DisplayName("ALLOW is terminal decision")
    void allowIsTerminal() {
        assertTrue(PermissionDecision.ALLOW.isTerminal());
    }

    @Test
    @DisplayName("REQUIRE_APPROVAL is not terminal")
    void requireApprovalIsNotTerminal() {
        assertFalse(PermissionDecision.REQUIRE_APPROVAL.isTerminal());
    }
}
