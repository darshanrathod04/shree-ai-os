package platform.core.health.model;

import java.util.Objects;

/**
 * <b>HealthComponent</b>
 *
 * <p>Represents a platform component for health monitoring within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Identifies a platform component for health monitoring.</li>
 *   <li>Provides type-safe identity, name, and category for component classification.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All fields are non-null.</p>
 */
public final class HealthComponent {

    private final HealthComponentId id;
    private final String name;
    private final String category;

    /**
     * Constructs a new {@code HealthComponent} with the given parameters.
     *
     * @param id       the component ID (must not be null)
     * @param name     the component name (must not be null)
     * @param category the component category (must not be null)
     * @throws IllegalArgumentException if any required parameter is null
     */
    public HealthComponent(HealthComponentId id, String name, String category) {
        this.id = Objects.requireNonNull(id, "HealthComponentId must not be null");
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.category = Objects.requireNonNull(category, "Category must not be null");
    }

    /**
     * Returns the component ID.
     *
     * @return the component ID
     */
    public HealthComponentId id() {
        return id;
    }

    /**
     * Returns the component name.
     *
     * @return the component name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the component category.
     *
     * @return the component category
     */
    public String category() {
        return category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthComponent that = (HealthComponent) o;
        return id.equals(that.id)
                && name.equals(that.name)
                && category.equals(that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, category);
    }

    @Override
    public String toString() {
        return "HealthComponent{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", category='" + category + '\''
                + '}';
    }
}