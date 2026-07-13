/**
 * <b>Configuration Error Architecture</b>
 *
 * <p>Defines all standard errors used by the Configuration subsystem within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a complete error architecture for the Configuration subsystem.</li>
 *   <li>Ensures consistent error reporting across all Configuration operations.</li>
 *   <li>Follows the Platform-wide error pattern established by other subsystems.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.configuration.error
 * ├── ConfigurationErrorCode.java       — Enum of error codes
 * ├── ConfigurationError.java           — Immutable error model
 * ├── ConfigurationException.java       — Base runtime exception
 * ├── DuplicateConfigurationException.java — Duplicate configuration error
 * ├── ConfigurationNotFoundException.java  — Configuration not found error
 * ├── InvalidConfigurationException.java    — Invalid configuration error
 * ├── package-info.java
 * └── README.md
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Immutable error model — all error models are immutable value objects.</li>
 *   <li>No business logic — errors are pure data carriers.</li>
 *   <li>No Spring annotations — framework-agnostic.</li>
 *   <li>No persistence annotations — no ORM mappings.</li>
 *   <li>No Lombok — explicit constructors and getters.</li>
 *   <li>Constructor validation — all invariants enforced at construction time.</li>
 * </ul>
 *
 * <p><b>Error Principle:</b> Every Platform Core Service owns its own Error Architecture.
 * All Error Architectures SHALL follow one Platform pattern.</p>
 *
 * @see platform.core.configuration.error.ConfigurationErrorCode
 * @see platform.core.configuration.error.ConfigurationError
 * @see platform.core.configuration.error.ConfigurationException
 */
package platform.core.configuration.error;