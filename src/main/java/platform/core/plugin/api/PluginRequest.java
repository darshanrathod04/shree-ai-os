package platform.core.plugin.api;

/**
 * <b>PluginRequest</b>
 *
 * <p>Immutable value object representing a plugin operation request within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates parameters for plugin operations.</li>
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
 * @see PluginService
 */
public final class PluginRequest {

    private final Plugin plugin;
    private final boolean deep;

    /**
     * Constructs a new {@code PluginRequest}.
     *
     * @param plugin the plugin
     * @param deep whether to perform deep operation
     * @throws IllegalArgumentException if plugin is null
     */
    public PluginRequest(Plugin plugin, boolean deep) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin must not be null");
        }
        this.plugin = plugin;
        this.deep = deep;
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
     * Returns whether to perform deep operation.
     *
     * @return true if deep operation, false otherwise
     */
    public boolean deep() {
        return deep;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two requests are equal if their plugins and deep flags are equal.</p>
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
        PluginRequest that = (PluginRequest) obj;
        return deep == that.deep && plugin.equals(that.plugin);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        int result = plugin.hashCode();
        result = 31 * result + (deep ? 1 : 0);
        return result;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "PluginRequest{" +
                "plugin=" + plugin +
                ", deep=" + deep +
                '}';
    }
}