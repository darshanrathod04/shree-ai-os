package platform.core.registry.service;

import platform.core.registry.api.KernelRegistry;
import platform.core.registry.error.DuplicateKernelException;
import platform.core.registry.error.InvalidKernelException;
import platform.core.registry.error.KernelNotFoundException;
import platform.core.registry.model.KernelId;
import platform.core.registry.model.RegisteredKernel;
import platform.core.registry.validator.KernelRegistrationValidator;
import platform.core.registry.validator.ValidationResult;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>DefaultKernelRegistry</b>
 *
 * <p>Default in-memory implementation of the {@link KernelRegistry} interface.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the official reference implementation for the Kernel Registry.</li>
 *   <li>Owns the registry storage and enforces all registration, unregistration, and lookup operations.</li>
 *   <li>Ensures thread-safe access to registered kernels.</li>
 *   <li>Never bypasses validation or error architecture.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> The registry is the source of truth for registered kernels.
 * All operations are atomic and thread-safe.</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-005, KERNEL-007, ADD-PLT-202,
 * ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Registration Flow:</b></p>
 * <pre>
 * register()
 *   ↓
 * Validate
 *   ↓
 * Duplicate Check
 *   ↓
 * Store
 *   ↓
 * Return Result
 * </pre>
 *
 * <p><b>Unregistration Flow:</b></p>
 * <pre>
 * unregister()
 *   ↓
 * Exists?
 *   ↓
 * Remove
 *   ↓
 * Return Result
 * </pre>
 *
 * <p><b>Lookup Flow:</b></p>
 * <pre>
 * find()
 *   ↓
 * Lookup
 *   ↓
 * Optional Result
 * </pre>
 *
 * @see KernelRegistry
 * @see KernelRegistrationValidator
 * @see ValidationResult
 */
public final class DefaultKernelRegistry implements KernelRegistry<RegisteredKernel> {

    private final KernelRegistrationValidator validator;
    private final Map<KernelId, RegisteredKernel> registry;

    /**
     * Constructs a new {@code DefaultKernelRegistry} with the given validator.
     *
     * <p>Uses constructor injection only. The validator is required and must not be null.</p>
     *
     * @param validator the kernel registration validator (must not be null)
     * @throws NullPointerException if {@code validator} is null
     */
    public DefaultKernelRegistry(KernelRegistrationValidator validator) {
        this.validator = validator;
        this.registry = new ConcurrentHashMap<>();
    }

    /**
     * Registers a kernel with the platform.
     *
     * <p><b>Registration Flow:</b></p>
     * <ol>
     *   <li>Validate the kernel using the validator</li>
     *   <li>Check for duplicate kernel identifier</li>
     *   <li>Store the kernel in the registry</li>
     *   <li>Return success result</li>
     * </ol>
     *
     * <p>Throws {@link InvalidKernelException} if validation fails.
     * Throws {@link DuplicateKernelException} if the kernel is already registered.</p>
     *
     * @param kernelId the unique identifier for the kernel to register
     * @param entry    the registration entry containing kernel metadata and contracts
     * @return {@code true} if the kernel was successfully registered
     * @throws IllegalArgumentException if {@code kernelId} or {@code entry} is {@code null}
     * @throws InvalidKernelException if validation fails
     * @throws DuplicateKernelException if the kernel is already registered
     */
    @Override
    public boolean register(String kernelId, RegisteredKernel entry) {
        if (kernelId == null) {
            throw new IllegalArgumentException("KernelId must not be null");
        }
        if (entry == null) {
            throw new IllegalArgumentException("RegisteredKernel must not be null");
        }

        // Step 1: Validate the kernel
        ValidationResult validationResult = validator.validate(entry);
        if (!validationResult.isValid()) {
            String errorMessage = String.join("; ", validationResult.errors());
            throw new InvalidKernelException(errorMessage);
        }

        // Step 2: Check for duplicate
        KernelId id = entry.kernelId();
        RegisteredKernel existing = registry.get(id);
        if (existing != null) {
            throw new DuplicateKernelException(id.value());
        }

        // Step 3: Store the kernel
        registry.put(id, entry);

        // Step 4: Return success
        return true;
    }

    /**
     * Unregisters a kernel from the platform.
     *
     * <p><b>Unregistration Flow:</b></p>
     * <ol>
     *   <li>Check if the kernel exists</li>
     *   <li>Remove the kernel from the registry</li>
     *   <li>Return success result</li>
     * </ol>
     *
     * @param kernelId the unique identifier of the kernel to unregister
     * @return {@code true} if the kernel was successfully unregistered,
     *         {@code false} if no kernel with the given identifier was found
     * @throws IllegalArgumentException if {@code kernelId} is {@code null}
     */
    @Override
    public boolean unregister(String kernelId) {
        if (kernelId == null) {
            throw new IllegalArgumentException("KernelId must not be null");
        }

        // Parse the kernel ID
        KernelId id = new KernelId(kernelId);

        // Check if exists and remove
        RegisteredKernel removed = registry.remove(id);
        return removed != null;
    }

    /**
     * Finds a registered kernel by its unique identifier.
     *
     * <p><b>Lookup Flow:</b></p>
     * <ol>
     *   <li>Lookup the kernel in the registry</li>
     *   <li>Return Optional result</li>
     * </ol>
     *
     * @param kernelId the unique identifier of the kernel to find
     * @return an {@link Optional} containing the registration entry if found,
     *         or an empty {@link Optional} if not found
     * @throws IllegalArgumentException if {@code kernelId} is {@code null}
     */
    @Override
    public Optional<RegisteredKernel> find(String kernelId) {
        if (kernelId == null) {
            throw new IllegalArgumentException("KernelId must not be null");
        }

        // Parse the kernel ID
        KernelId id = new KernelId(kernelId);

        // Lookup and return Optional
        RegisteredKernel found = registry.get(id);
        return Optional.ofNullable(found);
    }

    /**
     * Returns all currently registered kernels.
     *
     * <p>Returns an unmodifiable view of all registered kernels.
     * The returned collection is a snapshot at the time of the call.</p>
     *
     * @return an unmodifiable collection of all registration entries;
     *         returns an empty collection if no kernels are registered
     */
    @Override
    public Collection<RegisteredKernel> findAll() {
        // Return unmodifiable view to prevent external modification
        return Collections.unmodifiableCollection(registry.values());
    }

    /**
     * Checks whether a kernel with the given identifier is currently registered.
     *
     * @param kernelId the unique identifier of the kernel to check
     * @return {@code true} if a kernel with the given identifier is registered,
     *         {@code false} otherwise
     * @throws IllegalArgumentException if {@code kernelId} is {@code null}
     */
    @Override
    public boolean exists(String kernelId) {
        if (kernelId == null) {
            throw new IllegalArgumentException("KernelId must not be null");
        }

        // Parse the kernel ID
        KernelId id = new KernelId(kernelId);

        // Check existence
        return registry.containsKey(id);
    }
}