package com.shreeai.os.platform.runtime.execution;

/**
 * <b>PermissionDecision</b>
 *
 * <p>Enumerates the permission decisions evaluated by the Runtime before any
 * capability-driven execution is dispatched to a kernel.</p>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Version:</b> 2.1</p>
 *
 * @since 2.1
 */
public enum PermissionDecision {

    /** Execution is allowed without further gating. */
    ALLOW("ALLOW"),

    /** Execution requires an explicit approval before dispatching. */
    REQUIRE_APPROVAL("REQUIRE_APPROVAL"),

    /** Execution is denied. The Runtime must stop and not dispatch. */
    DENY("DENY");

    private final String value;

    PermissionDecision(String value) {
        this.value = value;
    }

    /**
     * Returns the canonical string value of this decision.
     *
     * @return the canonical value (never null)
     */
    public String value() {
        return value;
    }

    /**
     * Returns whether this decision is terminal for the request.
     *
     * <p>Only {@code DENY} and {@code ALLOW} are terminal; {@code
     * REQUIRE_APPROVAL} requires a secondary approval evaluation.</p>
     *
     * @return true when the decision does not require approval
     */
    public boolean isTerminal() {
        return this == ALLOW || this == DENY;
    }
}
