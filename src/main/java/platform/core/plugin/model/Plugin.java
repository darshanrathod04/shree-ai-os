package platform.core.plugin.model;

/**
 * <b>Plugin</b>
 *
 * <p>Immutable value object representing a plugin within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Represents a plugin with identity, name, and version.</li>
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
 *   <li>Value equality.</li>
 *   <li>No business logic.</li>
 *   <li>No persistence.</li>
 * </ul>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301, STD-003</p>
 *
 * @see PluginId
 * @see PluginDescriptor
 * @see PluginService
 */
public final class Plugin {

    private final PluginId id;
    private final String name;
    private final String version;

    /**
     * Constructs a new {@code Plugin}.
     *
     * @param id the plugin identifier
     * @param name the plugin name
     * @param version the plugin version
     * @throws IllegalArgumentException if any parameter is null or blank
     */
    public Plugin(PluginId id, String name, String version) {
        if (id == null) {
            throw new IllegalArgumentException("PluginId must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Plugin name must not be null or blank");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("Plugin version must not be null or blank");
        }
        this.id = id;
        this.name = name;
        this.version = version;
    }

    /**
     * Returns the plugin identifier.
     *
     * @return the plugin id
     */
    public PluginId id() {
        return id;
    }

    /**
     * Returns the plugin name.
     *
     * @return the plugin name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the plugin version.
     *
     * @return the plugin version
     */
    public String version() {
        return version;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two plugins are equal if their ids, names, and versions are equal.</p>
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
        Plugin plugin = (Plugin) obj;
        return id.equals(plugin.id) &&
                name.equals(plugin.name) &&
                version.equals(plugin.version);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "Plugin{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}