package platform.core.configuration.error;

/**
 * <b>ConfigurationErrorCode</b>
 *
 * <p>Enumeration of all possible Configuration subsystem error conditions within Shree AI OS.</p>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum ConfigurationErrorCode {

    /**
     * A configuration with the same key already exists.
     */
    CONFIGURATION_DUPLICATE,

    /**
     * The requested configuration was not found.
     */
    CONFIGURATION_NOT_FOUND,

    /**
     * The configuration is invalid.
     */
    CONFIGURATION_INVALID,

    /**
     * The configuration validation failed.
     */
    CONFIGURATION_VALIDATION_FAILED,

    /**
     * The configuration is read-only and cannot be modified.
     */
    CONFIGURATION_READ_ONLY,

    /**
     * The configuration namespace was not found.
     */
    CONFIGURATION_NAMESPACE_NOT_FOUND,

    /**
     * The configuration value type does not match the expected type.
     */
    CONFIGURATION_TYPE_MISMATCH
}