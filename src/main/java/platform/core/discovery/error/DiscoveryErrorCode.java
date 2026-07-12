package platform.core.discovery.error;

/**
 * <b>DiscoveryErrorCode</b>
 *
 * <p>Standardized error codes for the Discovery Service within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a typed enumeration of all possible discovery error conditions.</li>
 *   <li>Enables consistent error handling across the Discovery Service.</li>
 *   <li>Mirrors the Registry Error Architecture pattern.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-006</p>
 *
 * @see DiscoveryError
 * @see DiscoveryException
 */
public enum DiscoveryErrorCode {

    /**
     * The requested capability was not found in the platform.
     */
    DISCOVERY_CAPABILITY_NOT_FOUND,

    /**
     * The requested contract was not found in the platform.
     */
    DISCOVERY_CONTRACT_NOT_FOUND,

    /**
     * The discovery request is invalid or malformed.
     */
    DISCOVERY_INVALID_REQUEST,

    /**
     * The requested capability or contract version is incompatible.
     */
    DISCOVERY_INCOMPATIBLE_VERSION,

    /**
     * Discovery validation failed — the request does not satisfy discovery prerequisites.
     */
    DISCOVERY_VALIDATION_FAILED
}