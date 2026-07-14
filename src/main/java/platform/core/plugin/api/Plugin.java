package platform.core.plugin.api;

/**
 * <b>Plugin</b>
 *
 * <p>Immutable value object representing a plugin identity within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Represents a unique plugin identifier.</li>
 *   <li>Contains no business logic.</li>
 *   <li>Immutable by design.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Immutable — final class, final field.</li>
 *   <li>Constructor validation.</li>
 *   <li>No business logic.</li>
 *   <li>No persistence.</li>
 * </ul>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * @see PluginDescriptor
 * @see PluginService
 */
public final class Plugin {

    private final String value;

    /**
     * Constructs a new {@code Plugin}.
     *
     * @param value the plugin identifier
     * @throws IllegalArgumentException if value is null or blank
     */
    public Plugin(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Plugin value must not be null or blank");
        }
        this.value = value;
    }

    /**
     * Returns the plugin identifier.
     *
     * @return the plugin value
     */
    public String value() {
        return value;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two plugins are equal if their values are equal.</p>
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
        return value.equals(plugin.value);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        return "Plugin{" +
                "value='" + value + '\'' +
                '}';
    }
}