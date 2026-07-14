package platform.core.health.service;

import platform.core.health.api.HealthService;
import platform.core.health.error.HealthCheckFailedException;
import platform.core.health.error.HealthComponentNotFoundException;
import platform.core.health.error.HealthErrorCode;
import platform.core.health.error.HealthError;
import platform.core.health.error.HealthException;
import platform.core.health.error.InvalidHealthComponentException;
import platform.core.health.engine.HealthEvaluationEngine;
import platform.core.health.model.HealthCheck;
import platform.core.health.model.HealthComponent;
import platform.core.health.model.HealthComponentId;
import platform.core.health.model.HealthReport;
import platform.core.health.validator.HealthValidator;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>DefaultHealthService</b>
 *
 * <p>The default in-memory implementation of the {@link HealthService} contract
 * within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Implements the HealthService contract.</li>
 *   <li>Owns the health component storage.</li>
 *   <li>Coordinates validation and health evaluation.</li>
 *   <li>Ensures thread-safe health management.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Engineering Principle:</b> HealthService coordinates. HealthValidator validates.
 * HealthEvaluationEngine evaluates. HealthError reports failures.
 * These responsibilities shall remain independent forever.</p>
 *
 * @see HealthService
 * @see HealthValidator
 * @see HealthEvaluationEngine
 */
public final class DefaultHealthService implements HealthService {

    private final HealthValidator validator;
    private final HealthEvaluationEngine engine;
    private final Map<HealthComponentId, HealthComponent> components;

    /**
     * Constructs a new {@code DefaultHealthService} with the given validator and engine.
     *
     * @param validator the health validator (must not be null)
     * @param engine the health evaluation engine (must not be null)
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public DefaultHealthService(HealthValidator validator, HealthEvaluationEngine engine) {
        this.validator = Objects.requireNonNull(validator, "HealthValidator must not be null");
        this.engine = Objects.requireNonNull(engine, "HealthEvaluationEngine must not be null");
        this.components = new ConcurrentHashMap<>();
    }

    /**
     * Registers a health component for monitoring.
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Validate component</li>
     *   <li>Reject duplicates</li>
     *   <li>Store component</li>
     *   <li>Return true</li>
     * </ol>
     *
     * @param component the health component to register (must not be null)
     * @return {@code true} if registration succeeded
     * @throws IllegalArgumentException if {@code component} is {@code null}
     * @throws InvalidHealthComponentException if the component is invalid
     * @throws HealthException if the component is already registered
     */
    @Override
    public boolean register(HealthComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("HealthComponent must not be null");
        }

        // Step 1: Validate component
        var validationResult = validator.validateComponent(component);
        if (!validationResult.isValid()) {
            throw new InvalidHealthComponentException(
                    component,
                    String.join(", ", validationResult.errors())
            );
        }

        // Step 2: Reject duplicates
        HealthComponentId id = component.id();
        if (components.containsKey(id)) {
            throw new HealthException(
                    new HealthError(
                            HealthErrorCode.HEALTH_ALREADY_REGISTERED,
                            "Health component with id '" + id.value() + "' is already registered",
                            Instant.now(),
                            Map.of("componentId", id.value())
                    )
            );
        }

        // Step 3: Store component
        components.put(id, component);

        // Step 4: Return true
        return true;
    }

    /**
     * Checks the health of a specific component.
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Validate component</li>
     *   <li>Lookup component</li>
     *   <li>Delegate to HealthEvaluationEngine</li>
     *   <li>Return HealthReport</li>
     * </ol>
     *
     * @param component the health component to check (must not be null)
     * @return an {@code Optional} containing the health report if the component
     *         is registered, or an empty {@code Optional} if not found
     * @throws IllegalArgumentException if {@code component} is {@code null}
     * @throws HealthCheckFailedException if the health check fails
     */
    @Override
    public Optional<HealthReport> check(HealthComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("HealthComponent must not be null");
        }

        // Step 1: Validate component
        var validationResult = validator.validateComponent(component);
        if (!validationResult.isValid()) {
            throw new InvalidHealthComponentException(
                    component,
                    String.join(", ", validationResult.errors())
            );
        }

        // Step 2: Lookup component
        HealthComponentId id = component.id();
        if (!components.containsKey(id)) {
            return Optional.empty();
        }

        // Step 3: Delegate to HealthEvaluationEngine
        HealthReport report = engine.evaluate(component, false);

        // Step 4: Return HealthReport
        return Optional.of(report);
    }

    /**
     * Checks the health of all registered components.
     *
     * <p>The returned collection is a snapshot of the health reports at the time of the call.
     * It SHALL be unmodifiable.</p>
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Iterate registered components</li>
     *   <li>Delegate every component to HealthEvaluationEngine</li>
     *   <li>Collect reports</li>
     *   <li>Return immutable collection</li>
     * </ol>
     *
     * @return an unmodifiable collection of health reports for all registered components;
     *         returns an empty collection if no components are registered
     */
    @Override
    public Collection<HealthReport> checkAll() {
        // Step 1 & 2 & 3: Iterate, delegate, and collect reports
        var reports = new java.util.ArrayList<HealthReport>();
        for (HealthComponent component : components.values()) {
            try {
                HealthReport report = engine.evaluate(component, false);
                reports.add(report);
            } catch (HealthCheckFailedException e) {
                // Log and continue with other components
                // Individual component failures should not prevent checking other components
            }
        }

        // Step 4: Return immutable collection
        return Collections.unmodifiableCollection(reports);
    }

    /**
     * Unregisters a health component from monitoring.
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Validate component</li>
     *   <li>Remove component</li>
     *   <li>Return success</li>
     * </ol>
     *
     * @param component the health component to unregister (must not be null)
     * @return {@code true} if a component was unregistered, {@code false} otherwise
     * @throws IllegalArgumentException if {@code component} is {@code null}
     */
    @Override
    public boolean unregister(HealthComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("HealthComponent must not be null");
        }

        // Step 1: Validate component
        var validationResult = validator.validateComponent(component);
        if (!validationResult.isValid()) {
            return false;
        }

        // Step 2: Remove component
        HealthComponentId id = component.id();
        HealthComponent removed = components.remove(id);

        // Step 3: Return success
        return removed != null;
    }

    /**
     * Returns whether a health component exists and is registered for monitoring.
     *
     * <p>This is a lookup-only operation — no validation is performed.</p>
     *
     * @param component the health component to check (must not be null)
     * @return {@code true} if a component exists and is registered, {@code false} otherwise
     * @throws IllegalArgumentException if {@code component} is {@code null}
     */
    @Override
    public boolean exists(HealthComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("HealthComponent must not be null");
        }

        // Lookup only
        return components.containsKey(component.id());
    }
}