package com.shreeai.os.platform.runtime.execution;

import com.shreeai.os.platform.kernels.cognitive.engine.DefaultReflectionEngine;
import com.shreeai.os.platform.kernels.memory.api.MemorySearchService;
import com.shreeai.os.platform.kernels.tool.api.ToolService;
import com.shreeai.os.platform.security.api.ApprovalService;

import java.util.Objects;

/**
 * Bootstrap component that wires together the {@link KernelRegistry},
 * {@link DefaultPermissionPolicy}, {@link ExecutionDispatcher}, and all
 * kernel handlers, producing a ready-to-use dispatch layer.
 *
 * <p>This is the entry point for the V2.1 autonomous execution dispatch
 * infrastructure. It registers a {@link KernelHandler} for each
 * {@link ExecutionCapability} and configures default permission policies.</p>
 *
 * @since 2.1
 */
public final class RuntimeKernelBootstrap {

    private final KernelRegistry kernelRegistry;
    private final DefaultPermissionPolicy permissionPolicy;
    private final ExecutionDispatcher dispatcher;
    private final MemorySearchService memorySearchService;
    private final ToolService toolService;
    private final DefaultReflectionEngine reflectionEngine;

    private boolean initialized = false;

    public RuntimeKernelBootstrap(
            MemorySearchService memorySearchService,
            ToolService toolService,
            DefaultReflectionEngine reflectionEngine) {

        this.memorySearchService = Objects.requireNonNull(
                memorySearchService, "memorySearchService must not be null");
        this.toolService = Objects.requireNonNull(
                toolService, "toolService must not be null");
        this.reflectionEngine = Objects.requireNonNull(
                reflectionEngine, "reflectionEngine must not be null");

        this.kernelRegistry = new KernelRegistry();
        this.permissionPolicy = new DefaultPermissionPolicy(PermissionDecision.ALLOW);
        this.dispatcher = new ExecutionDispatcher(kernelRegistry, permissionPolicy);
    }

    /**
     * Registers all kernel handlers for the known capabilities.
     *
     * @return this bootstrap instance for method chaining
     */
    public RuntimeKernelBootstrap registerAllHandlers() {
        // Memory recall handler
        kernelRegistry.register(
                ExecutionCapability.MEMORY_RECALL,
                new MemoryKernelHandler(memorySearchService));

        // Task execution handler (tool-backed)
        kernelRegistry.register(
                ExecutionCapability.TASK_EXECUTION,
                new ToolRegistryKernelHandler(toolService));

        // Reflection handler - used for post-execution analysis and
        // reflection-driven autonomous retry. Registered for KNOWLEDGE_SEARCH,
        // PROJECT_PLANNING, and WORKOUT_PLANNING as a deterministic fallback
        // when no dedicated handler is injected.
        // NOTE: For production use, inject dedicated Knowledge and Planning
        // handlers via register() after bootstrap initialization.
        kernelRegistry.register(
                ExecutionCapability.KNOWLEDGE_SEARCH,
                new ReflectionKernelHandler(reflectionEngine));

        kernelRegistry.register(
                ExecutionCapability.PROJECT_PLANNING,
                new ReflectionKernelHandler(reflectionEngine));
        kernelRegistry.register(
                ExecutionCapability.WORKOUT_PLANNING,
                new ReflectionKernelHandler(reflectionEngine));

        initialized = true;
        return this;
    }

    /**
     * Sets a permission decision for a capability.
     *
     * @param capability the capability
     * @param decision   the permission decision
     * @return this bootstrap instance for method chaining
     */
    public RuntimeKernelBootstrap setPermission(
            ExecutionCapability capability,
            PermissionDecision decision) {

        permissionPolicy.set(capability, decision);
        return this;
    }

    /**
     * Returns the wire-up {@link ExecutionDispatcher}.
     *
     * @return the dispatcher (never null)
     * @throws IllegalStateException if handlers have not been registered
     */
    public ExecutionDispatcher getDispatcher() {
        if (!initialized) {
            throw new IllegalStateException(
                    "Bootstrap has not been initialized. Call registerAllHandlers() first.");
        }
        return dispatcher;
    }

    /**
     * Returns the {@link KernelRegistry} used by this bootstrap.
     *
     * @return the kernel registry (never null)
     */
    public KernelRegistry getKernelRegistry() {
        return kernelRegistry;
    }

    /**
     * Returns the {@link DefaultPermissionPolicy} used by this bootstrap.
     *
     * @return the permission policy (never null)
     */
    public DefaultPermissionPolicy getPermissionPolicy() {
        return permissionPolicy;
    }

    /**
     * Creates an {@link ApprovalIntegration} wired to the dispatcher and
     * the provided approval service.
     *
     * @param approvalService the approval service (must not be null)
     * @return a new approval integration
     * @throws IllegalStateException if bootstrap has not been initialized
     */
        public ApprovalIntegration createApprovalIntegration(ApprovalService approvalService) {
        Objects.requireNonNull(approvalService, "approvalService must not be null");
        if (!initialized) {
            throw new IllegalStateException(
                    "Bootstrap has not been initialized. Call registerAllHandlers() first.");
        }
        return new ApprovalIntegration(dispatcher, approvalService, permissionPolicy);
    }
}
