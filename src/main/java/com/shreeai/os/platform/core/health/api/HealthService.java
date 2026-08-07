package com.shreeai.os.platform.core.health.api;

import java.util.Collection;
import java.util.Optional;

import com.shreeai.os.platform.core.health.model.HealthComponent;
import com.shreeai.os.platform.core.health.model.HealthReport;

/**
 * <b>HealthService</b>
 *
 * <p>The public contract for Platform health monitoring within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the Platform contract for health monitoring operations.</li>
 *   <li>Specifies WHAT the Platform can do — implementations define HOW.</li>
 *   <li>Ensures health monitoring is independent of implementation details.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Health Principle:</b> The Health API defines WHAT the Platform can do.
 * Future services define HOW the Platform does it.</p>
 *
 * @see platform.core.health.api package-info
 */
public interface HealthService {

    /**
     * Registers a health component for monitoring.
     *
     * <p>If a component with the same identity already exists, it SHALL be replaced.</p>
     *
     * @param component the health component to register (must not be null)
     * @return {@code true} if registration succeeded, {@code false} otherwise
     * @throws IllegalArgumentException if {@code component} is {@code null}
     */
    boolean register(HealthComponent component);

    /**
     * Checks the health of a specific component.
     *
     * @param component the health component to check (must not be null)
     * @return an {@code Optional} containing the health report if the component
     *         is registered, or an empty {@code Optional} if not found
     * @throws IllegalArgumentException if {@code component} is {@code null}
     */
    Optional<HealthReport> check(HealthComponent component);

    /**
     * Checks the health of all registered components.
     *
     * <p>The returned collection is a snapshot of the health reports at the time of the call.
     * It SHALL be unmodifiable.</p>
     *
     * @return an unmodifiable collection of health reports for all registered components;
     *         returns an empty collection if no components are registered
     */
    Collection<HealthReport> checkAll();

    /**
     * Unregisters a health component from monitoring.
     *
     * <p>If no component with the given identity is registered, this method SHALL
     * return {@code false}.</p>
     *
     * @param component the health component to unregister (must not be null)
     * @return {@code true} if a component was unregistered, {@code false} otherwise
     * @throws IllegalArgumentException if {@code component} is {@code null}
     */
    boolean unregister(HealthComponent component);

    /**
     * Returns whether a health component exists and is registered for monitoring.
     *
     * @param component the health component to check (must not be null)
     * @return {@code true} if a component exists and is registered, {@code false} otherwise
     * @throws IllegalArgumentException if {@code component} is {@code null}
     */
    boolean exists(HealthComponent component);
}