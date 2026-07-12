package platform.core.discovery.model;

/**
 * <b>ResolutionStatus</b>
 *
 * <p>Represents the result of capability resolution within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a typed enumeration of all possible capability resolution outcomes.</li>
 *   <li>Enables consistent status reporting across the Discovery Service.</li>
 *   <li>Supports the Discovery Service's resolution mechanism.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> KERNEL-006</p>
 *
 * @see platform.core.discovery.model.DiscoveryResult
 */
public enum ResolutionStatus {

    /**
     * The capability was found and is available.
     */
    FOUND,

    /**
     * The capability was not found in the platform.
     */
    NOT_FOUND,

    /**
     * The capability was found but is incompatible with the request.
     */
    INCOMPATIBLE,

    /**
     * The capability was found but is currently unavailable (e.g., suspended or retired).
     */
    UNAVAILABLE
}