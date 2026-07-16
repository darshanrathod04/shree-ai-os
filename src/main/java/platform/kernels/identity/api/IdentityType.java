package platform.kernels.identity.api;

/**
 * <b>IdentityType</b>
 *
 * <p>Defines the type of an Identity within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a stable enumeration of Identity types.</li>
 *   <li>Enables type-safe classification of Identities.</li>
 *   <li>Supports platform-wide identity categorization.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Identity Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is a pure enumeration with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-104</p>
 */
public enum IdentityType {
    /** A human user of the platform */
    HUMAN,
    /** An AI agent operating within the platform */
    AGENT,
    /** An organization or company */
    ORGANIZATION,
    /** A physical or virtual device */
    DEVICE,
    /** A platform service */
    SERVICE,
    /** A plugin or extension */
    PLUGIN
}