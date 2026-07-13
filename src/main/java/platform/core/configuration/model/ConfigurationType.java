package platform.core.configuration.model;

/**
 * <b>ConfigurationType</b>
 *
 * <p>Defines the supported types for configuration values within Shree AI OS.</p>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum ConfigurationType {

    /**
     * String configuration value.
     */
    STRING,

    /**
     * Integer configuration value.
     */
    INTEGER,

    /**
     * Boolean configuration value.
     */
    BOOLEAN,

    /**
     * Double configuration value.
     */
    DOUBLE,

    /**
     * List configuration value.
     */
    LIST,

    /**
     * Map configuration value.
     */
    MAP
}