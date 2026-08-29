package com.shreeai.os.platform.runtime.execution;

import java.util.Map;
import java.util.Objects;

/**
 * <b>ExecutionDispatcher</b>
 *
 * <p>Orchestrates capability-driven execution: evaluates the permission
 * policy, resolves the owning kernel handler from the {@link KernelRegistry},
 * and executes it to produce a {@link RichExecutionResult}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Routes capability requests to the owning kernel handler.</li>
 *   <li>Enforces the permission policy before dispatching.</li>
 *   <li>Stops execution immediately on {@code DENY}.</li>
 *   <li>Contains no kernel logic and never calls the SDK.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Version:</b> 2.1</p>
 *
 * @since 2.1
 */
public final class ExecutionDispatcher {

    private final KernelRegistry kernelRegistry;
    private final PermissionPolicy permissionPolicy;

    /**
     * Creates an {@code ExecutionDispatcher}.
     *
     * @param kernelRegistry   the kernel registry (never null)
     * @param permissionPolicy the permission policy (never null)
     * @throws IllegalArgumentException if any dependency is null
     */
    public ExecutionDispatcher(
            KernelRegistry kernelRegistry,
            PermissionPolicy permissionPolicy) {
        this.kernelRegistry = Objects.requireNonNull(
                kernelRegistry, "kernelRegistry must not be null");
        this.permissionPolicy = Objects.requireNonNull(
                permissionPolicy, "permissionPolicy must not be null");
    }

    /**
     * Dispatches a capability execution.
     *
     * <p>Flow: permission evaluation → handler resolution → execution.</p>
     *
     * <p>When the permission policy returns {@code DENY}, execution stops
     * immediately and a denied result is returned. When the policy returns
     * {@code REQUIRE_APPROVAL}, a pending-approval result is returned.</p>
     *
     * @param capability the capability to dispatch (never null)
     * @param input      the request payload (never null; may be blank)
     * @param context    the execution context (never null; may be empty)
     * @return the rich execution result (never null)
     * @throws IllegalArgumentException if capability is null
     */
    public RichExecutionResult dispatch(
            ExecutionCapability capability,
            String input,
            Map<String, Object> context) {
        if (capability == null) {
            throw new IllegalArgumentException("capability must not be null");
        }

        // 1. Permission gate
        PermissionDecision decision = permissionPolicy.evaluate(capability);

        if (decision == PermissionDecision.DENY) {
            return RichExecutionResult.denied(
                    capability,
                    "Execution denied by permission policy");
        }

        if (decision == PermissionDecision.REQUIRE_APPROVAL) {
            return RichExecutionResult.pendingApproval(
                    capability,
                    "Execution requires approval before dispatching");
        }

        // 2. Handler resolution
        return kernelRegistry.resolve(capability)
                .map(handler -> {
                    try {
                        return handler.handle(
                                capability,
                                input == null ? "" : input,
                                context == null ? Map.of() : context);
                    } catch (Exception e) {
                        return RichExecutionResult.failure(
                                capability,
                                "Handler execution failed: " + e.getMessage());
                    }
                })
                .orElseGet(() -> RichExecutionResult.failure(
                        capability,
                        "No handler registered for capability: "
                                + capability.value()));
    }

    /**
     * Returns whether the given capability is dispatchable (registered and allowed).
     *
     * @param capability the capability to check
     * @return true when the capability is registered and permitted
     */
    public boolean isDispatchable(ExecutionCapability capability) {
        return capability != null
                && kernelRegistry.isRegistered(capability)
                && permissionPolicy.evaluate(capability) == PermissionDecision.ALLOW;
    }
}
