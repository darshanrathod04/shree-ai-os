package platform.core.plugin.api;

/**
 * <b>PluginDescriptor</b>
 *
 * <p>Immutable value object describing a registered plugin within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides metadata about a registered plugin.</li>
 *   <li>Contains no business logic.</li>
 *   <li>Immutable by design.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Immutable — final class, final fields.</li>
 *   <li>Constructor validation.</li>
 *   <li>No business logic.</li>
 *   <li>No persistence.</li>
 * </ul>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * @see Plugin
 * @see PluginState
 * @see PluginService
 */
public final class PluginDescriptor {

    private final Plugin plugin;
    private final PluginState state;

    /**
     * Constructs a new {@code PluginDescriptor}.
     *
     * @param plugin the plugin
     * @param state the current plugin state
     * @throws IllegalArgumentException if plugin or state is null
     */
    public PluginDescriptor(Plugin plugin, PluginState state) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin must not be null");
        }
        if (state == null) {
            throw new IllegalArgumentException("PluginState must not be null");
        }
        this.plugin = plugin;
        this.state = state;
    }

    /**
     * Returns the plugin.
     *
     * @return the plugin
     */
    public Plugin plugin() {
        return plugin;
    }

    /**
     * Returns the current state.
     *
     * @return the plugin state
     */
    public PluginState state() {
        return state;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two descriptors are equal if their plugins and states are equal.</p>
     *
     * @param obj the reference object with which to compare
     * @return true if this object is the same as the obj argument
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PluginDescriptor that = (PluginDescriptor) obj;
        return plugin.equals(that.plugin) && state == that.state;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        int result = plugin.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "PluginDescriptor{" +
                "plugin=" + plugin +
                ", state=" + state +
                '}';
    }
}