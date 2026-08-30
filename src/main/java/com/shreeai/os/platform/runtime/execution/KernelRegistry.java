package com.shreeai.os.platform.runtime.execution;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>KernelRegistry</b>
 *
 * <p>Thread-safe registry that maps {@link ExecutionCapability} instances to
 * the {@link KernelHandler} implementations that own them. The registry is
 * populated during bootstrap and supports future plugin capabilities through
 * the {@link #register(ExecutionCapability, KernelHandler)} extension point.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Maps capabilities to kernel handlers.</li>
 *   <li>Supports runtime registration of plugin capabilities.</li>
 *   <li>Thread-safe: safe for concurrent register/resolve.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Version:</b> 2.1</p>
 *
 * @since 2.1
 */
public final class KernelRegistry {

    private final Map<ExecutionCapability, KernelHandler> registry =
            new ConcurrentHashMap<>();

    /**
     * Creates an empty registry.
     */
    public KernelRegistry() {
        // Start empty; bootstrap registers handlers.
    }

    /**
     * Registers a handler for the given capability.
     *
     * <p>Registering the same capability twice replaces the previous handler,
     * enabling OCP-compliant extension without modifying the dispatcher.</p>
     *
     * @param capability the capability (never null)
     * @param handler    the handler (never null)
     * @throws IllegalArgumentException if capability or handler is null
     */
    public void register(ExecutionCapability capability, KernelHandler handler) {
        if (capability == null) {
            throw new IllegalArgumentException("capability must not be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        }
        registry.put(capability, handler);
    }

    /**
     * Resolves the handler for the given capability.
     *
     * @param capability the capability (never null)
     * @return the registered handler, or empty when not registered
     */
    public Optional<KernelHandler> resolve(ExecutionCapability capability) {
        if (capability == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(registry.get(capability));
    }

    /**
     * Returns whether the given capability is registered.
     *
     * @param capability the capability (never null)
     * @return true when a handler is registered
     */
    public boolean isRegistered(ExecutionCapability capability) {
        return capability != null && registry.containsKey(capability);
    }

    /**
     * Returns the set of registered capabilities.
     *
     * @return an unmodifiable set (never null)
     */
    public Set<ExecutionCapability> registeredCapabilities() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    /**
     * Returns the number of registered capabilities.
     *
     * @return the registration count
     */
    public int size() {
        return registry.size();
    }
}
