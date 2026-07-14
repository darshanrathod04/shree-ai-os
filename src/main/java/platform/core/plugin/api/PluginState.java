package platform.core.plugin.api;

/**
 * <b>PluginState</b>
 *
 * <p>Enumeration of possible plugin states within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Represents the lifecycle state of a plugin.</li>
 *   <li>Contains no business logic.</li>
 *   <li>Immutable by design.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * @see PluginDescriptor
 * @see PluginService
 */
public enum PluginState {

    /**
     * Plugin has been loaded into memory but not started.
     */
    LOADED,

    /**
     * Plugin has been unloaded from memory.
     */
    UNLOADED,

    /**
     * Plugin is running and operational.
     */
    STARTED,

    /**
     * Plugin has been stopped but remains loaded.
     */
    STOPPED,

    /**
     * Plugin encountered an error during lifecycle operation.
     */
    FAILED
}