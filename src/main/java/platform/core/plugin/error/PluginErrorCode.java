package platform.core.plugin.error;

/**
 * <b>PluginErrorCode</b>
 *
 * <p>Enumeration of all possible Plugin subsystem error conditions within Shree AI OS.</p>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum PluginErrorCode {

    /**
     * A plugin with the same identifier is already registered.
     */
    PLUGIN_DUPLICATE,

    /**
     * The requested plugin was not found.
     */
    PLUGIN_NOT_FOUND,

    /**
     * The plugin is invalid.
     */
    PLUGIN_INVALID,

    /**
     * Plugin validation failed.
     */
    PLUGIN_VALIDATION_FAILED,

    /**
     * The plugin is already started.
     */
    PLUGIN_ALREADY_STARTED,

    /**
     * The plugin is already stopped.
     */
    PLUGIN_ALREADY_STOPPED,

    /**
     * The plugin lifecycle operation failed.
     */
    PLUGIN_LIFECYCLE_FAILED
}