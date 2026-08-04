package com.shreeai.os.platform.core.registry.api;

import java.util.Collection;
import java.util.Optional;

/**
 * <b>KernelRegistry</b>
 *
 * <p>The central registration authority for all kernels within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the public contract for kernel registration and discovery.</li>
 *   <li>Enforces the principle that no kernel participates in the platform
 *       until it is formally registered (KERNEL-005).</li>
 *   <li>Maintains a registry of known kernels and their associated metadata.</li>
 *   <li>Enables platform awareness, dependency validation, and contract discovery.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> A kernel MUST be registered before it can participate
 * in platform execution. Unregistered kernels cannot receive requests (KR-005).</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-005, ADD-PLT-202, ADD-PLT-205</p>
 *
 * @param <T> the type of registration entry used by the implementation
 *
 * @see platform.core.registry.api package-info
 */
public interface KernelRegistry<T> {

    /**
     * Registers a kernel with the platform.
     *
     * <p>Registration establishes a kernel's identity, capabilities, contracts,
     * and metadata with the platform. A kernel becomes a recognized platform
     * capability only after successful registration.</p>
     *
     * <p>Registration SHALL fail if:</p>
     * <ul>
     *   <li>A kernel with the same identifier is already registered (duplicate).</li>
     *   <li>The registration entry is invalid or incomplete.</li>
     * </ul>
     *
     * @param kernelId   the unique identifier for the kernel to register
     * @param entry      the registration entry containing kernel metadata and contracts
     * @return {@code true} if the kernel was successfully registered,
     *         {@code false} if registration was rejected
     * @throws IllegalArgumentException if {@code kernelId} or {@code entry} is {@code null}
     */
    boolean register(String kernelId, T entry);

    /**
     * Unregisters a kernel from the platform.
     *
     * <p>Removes the kernel from the registry, making it unavailable for
     * platform execution. After unregistration, the kernel SHALL no longer
     * be discoverable or able to receive requests.</p>
     *
     * @param kernelId the unique identifier of the kernel to unregister
     * @return {@code true} if the kernel was successfully unregistered,
     *         {@code false} if no kernel with the given identifier was found
     * @throws IllegalArgumentException if {@code kernelId} is {@code null}
     */
    boolean unregister(String kernelId);

    /**
     * Finds a registered kernel by its unique identifier.
     *
     * <p>Returns the registration entry associated with the given kernel identifier,
     * or an empty {@link Optional} if no such kernel is registered.</p>
     *
     * @param kernelId the unique identifier of the kernel to find
     * @return an {@link Optional} containing the registration entry if found,
     *         or an empty {@link Optional} if not found
     * @throws IllegalArgumentException if {@code kernelId} is {@code null}
     */
    Optional<T> find(String kernelId);

    /**
     * Returns all currently registered kernels.
     *
     * <p>Provides a snapshot of all kernels currently registered with the platform.
     * The returned collection is unmodifiable and reflects the state at the time
     * of the call.</p>
     *
     * @return an unmodifiable collection of all registration entries;
     *         returns an empty collection if no kernels are registered
     */
    Collection<T> findAll();

    /**
     * Checks whether a kernel with the given identifier is currently registered.
     *
     * <p>This is a convenience method equivalent to {@code find(kernelId).isPresent()}
     * but may be more efficient in implementations that support direct existence checks.</p>
     *
     * @param kernelId the unique identifier of the kernel to check
     * @return {@code true} if a kernel with the given identifier is registered,
     *         {@code false} otherwise
     * @throws IllegalArgumentException if {@code kernelId} is {@code null}
     */
    boolean exists(String kernelId);
}