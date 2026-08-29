package com.shreeai.os.platform.runtime.execution;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>DefaultPermissionPolicy</b>
 *
 * <p>Default thread-safe {@link PermissionPolicy} implementation that maps
 * capabilities to explicit permission decisions. Capabilities without an
 * explicit mapping default to {@link PermissionDecision#ALLOW}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides per-capability permission decisions.</li>
 *   <li>Defaults to ALLOW for unregistered capabilities.</li>
 *   <li>Supports dynamic policy updates.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Version:</b> 2.1</p>
 *
 * @since 2.1
 */
public final class DefaultPermissionPolicy implements PermissionPolicy {

    private final Map<ExecutionCapability, PermissionDecision> policies =
            new ConcurrentHashMap<>();

    /** Default decision applied to capabilities with no explicit mapping. */
    private final PermissionDecision defaultDecision;

    /**
     * Creates a policy that defaults to {@link PermissionDecision#ALLOW}.
     */
    public DefaultPermissionPolicy() {
        this(PermissionDecision.ALLOW);
    }

    /**
     * Creates a policy with an explicit default decision.
     *
     * @param defaultDecision the decision for unmapped capabilities (never null)
     */
    public DefaultPermissionPolicy(PermissionDecision defaultDecision) {
        this.defaultDecision = Objects.requireNonNull(
                defaultDecision, "defaultDecision must not be null");
    }

    @Override
    public PermissionDecision evaluate(ExecutionCapability capability) {
        Objects.requireNonNull(capability, "capability must not be null");
        return policies.getOrDefault(capability, defaultDecision);
    }

    /**
     * Sets the decision for a capability.
     *
     * @param capability the capability (never null)
     * @param decision   the decision (never null)
     */
    public void set(ExecutionCapability capability, PermissionDecision decision) {
        Objects.requireNonNull(capability, "capability must not be null");
        Objects.requireNonNull(decision, "decision must not be null");
        policies.put(capability, decision);
    }

    /**
     * Removes the explicit decision for a capability so that the default applies.
     *
     * @param capability the capability (never null)
     */
    public void clear(ExecutionCapability capability) {
        Objects.requireNonNull(capability, "capability must not be null");
        policies.remove(capability);
    }

    /**
     * Returns an unmodifiable view of the explicit policy mappings.
     *
     * @return unmodifiable map of capability → decision
     */
    public Map<ExecutionCapability, PermissionDecision> policies() {
        return Collections.unmodifiableMap(policies);
    }
}
