package platform.core.registry.error;

/**
 * <b>RegistryErrorCode</b>
 *
 * <p>Standardized error codes for the Kernel Registry within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a typed enumeration of all possible registry error conditions.</li>
 *   <li>Enables consistent error handling across the registry and future Platform Core Services.</li>
 *   <li>Supports error categorization without relying on string-based error messages.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-005, KERNEL-007</p>
 *
 * @see RegistryError
 * @see RegistryException
 */
public enum RegistryErrorCode {

    /**
     * A kernel with the same identifier is already registered.
     */
    REGISTRY_DUPLICATE_KERNEL,

    /**
     * The kernel data is structurally invalid or malformed.
     */
    REGISTRY_INVALID_KERNEL,

    /**
     * The requested kernel was not found in the registry.
     */
    REGISTRY_KERNEL_NOT_FOUND,

    /**
     * The kernel version is invalid or incompatible.
     */
    REGISTRY_INVALID_VERSION,

    /**
     * Kernel validation failed — the kernel does not satisfy registration prerequisites.
     */
    REGISTRY_VALIDATION_FAILED
}