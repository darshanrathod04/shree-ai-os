package com.shreeai.os.platform.core.plugin.service;

import com.shreeai.os.platform.core.plugin.api.PluginService;
import com.shreeai.os.platform.core.plugin.error.DuplicatePluginException;
import com.shreeai.os.platform.core.plugin.error.InvalidPluginException;
import com.shreeai.os.platform.core.plugin.model.Plugin;
import com.shreeai.os.platform.core.plugin.model.PluginDescriptor;
import com.shreeai.os.platform.core.plugin.model.PluginId;
import com.shreeai.os.platform.core.plugin.model.PluginState;
import com.shreeai.os.platform.core.plugin.engine.PluginLifecycleEngine;
import com.shreeai.os.platform.core.plugin.engine.PluginTransitionResult;
import com.shreeai.os.platform.core.plugin.validator.PluginValidator;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>DefaultPluginService</b>
 *
 * <p>The default in-memory implementation of the {@link PluginService} contract
 * within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Implements the PluginService contract.</li>
 *   <li>Owns the plugin storage.</li>
 *   <li>Coordinates validation and lifecycle management.</li>
 *   <li>Ensures thread-safe plugin management.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * <p><b>Engineering Principle:</b> DefaultPluginService coordinates. PluginValidator validates.
 * PluginLifecycleEngine manages lifecycle. PluginError reports failures.
 * These responsibilities shall remain independent forever.</p>
 *
 * @see PluginService
 * @see PluginValidator
 * @see PluginLifecycleEngine
 */
public final class DefaultPluginService implements PluginService {

    private final PluginValidator validator;
    private final PluginLifecycleEngine lifecycleEngine;
    private final Map<PluginId, PluginDescriptor> plugins;

    /**
     * Constructs a new {@code DefaultPluginService} with the given validator and lifecycle engine.
     *
     * @param validator the plugin validator (must not be null)
     * @param lifecycleEngine the plugin lifecycle engine (must not be null)
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public DefaultPluginService(PluginValidator validator, PluginLifecycleEngine lifecycleEngine) {
        this.validator = Objects.requireNonNull(validator, "PluginValidator must not be null");
        this.lifecycleEngine = Objects.requireNonNull(lifecycleEngine, "PluginLifecycleEngine must not be null");
        this.plugins = new ConcurrentHashMap<>();
    }

    /**
     * Registers a plugin with the framework.
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Validate plugin</li>
     *   <li>Reject duplicates</li>
     *   <li>Store plugin</li>
     *   <li>Return true</li>
     * </ol>
     *
     * @param plugin the plugin to register (must not be null)
     * @return {@code true} if registration succeeded
     * @throws IllegalArgumentException if {@code plugin} is {@code null}
     * @throws InvalidPluginException if the plugin is invalid
     * @throws DuplicatePluginException if the plugin is already registered
     */
    @Override
    public boolean register(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin must not be null");
        }

        // Step 1: Validate plugin
        var validationResult = validator.validatePlugin(plugin);
        if (!validationResult.isValid()) {
            throw new InvalidPluginException(plugin, String.join(", ", validationResult.errors()));
        }

        // Step 2: Reject duplicates
        PluginId id = plugin.id();
        if (plugins.containsKey(id)) {
            throw new DuplicatePluginException(plugin);
        }

        // Step 3: Store plugin
        PluginDescriptor descriptor = new PluginDescriptor(
                plugin,
                PluginState.LOADED,
                java.time.Instant.now(),
                "Platform Core"
        );
        plugins.put(id, descriptor);

        // Step 4: Return true
        return true;
    }

    /**
     * Retrieves plugin descriptor by plugin reference.
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Validate plugin</li>
     *   <li>Lookup map</li>
     *   <li>Return Optional</li>
     * </ol>
     *
     * @param plugin the plugin to retrieve (must not be null)
     * @return an {@code Optional} containing the plugin descriptor if found, or empty if not found
     * @throws IllegalArgumentException if {@code plugin} is {@code null}
     */
    @Override
    public Optional<PluginDescriptor> get(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin must not be null");
        }

        // Step 1: Validate plugin
        var validationResult = validator.validatePlugin(plugin);
        if (!validationResult.isValid()) {
            return Optional.empty();
        }

        // Step 2 & 3: Lookup map and return Optional
        PluginDescriptor descriptor = plugins.get(plugin.id());
        return Optional.ofNullable(descriptor);
    }

    /**
     * Lists all registered plugins.
     *
     * <p>The returned collection is a snapshot of the registered plugins at the time of the call.
     * It SHALL be unmodifiable.</p>
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Collect all descriptors</li>
     *   <li>Return unmodifiable collection</li>
     * </ol>
     *
     * @return an unmodifiable collection of plugin descriptors
     */
    @Override
    public Collection<PluginDescriptor> list() {
        // Step 1 & 2: Collect and return unmodifiable collection
        return Collections.unmodifiableCollection(plugins.values());
    }

    /**
     * Unregisters a plugin from the framework.
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Validate plugin</li>
     *   <li>Remove descriptor</li>
     *   <li>Return boolean</li>
     * </ol>
     *
     * @param plugin the plugin to unregister (must not be null)
     * @return {@code true} if the plugin was unregistered, {@code false} if not found
     * @throws IllegalArgumentException if {@code plugin} is {@code null}
     */
    @Override
    public boolean unregister(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin must not be null");
        }

        // Step 1: Validate plugin
        var validationResult = validator.validatePlugin(plugin);
        if (!validationResult.isValid()) {
            return false;
        }

        // Step 2: Remove descriptor
        PluginDescriptor removed = plugins.remove(plugin.id());

        // Step 3: Return boolean
        return removed != null;
    }

    /**
     * Checks if a plugin is registered.
     *
     * <p>This is a lookup-only operation — no validation is performed.</p>
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Lookup map</li>
     *   <li>Return boolean</li>
     * </ol>
     *
     * @param plugin the plugin to check (must not be null)
     * @return {@code true} if the plugin is registered, {@code false} otherwise
     * @throws IllegalArgumentException if {@code plugin} is {@code null}
     */
    @Override
    public boolean exists(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin must not be null");
        }

        // Step 1 & 2: Lookup and return boolean
        return plugins.containsKey(plugin.id());
    }

    // ========================================================================
    // Lifecycle Operations
    // ========================================================================
    //
    // The service coordinates; the engine executes transitions.
    // These methods delegate to PluginLifecycleEngine and update the stored
    // descriptor state on success.

    /**
     * Loads a plugin, transitioning it from UNLOADED to LOADED.
     *
     * <p>The service delegates the transition decision to the
     * {@link PluginLifecycleEngine} and updates the stored descriptor
     * state upon success.</p>
     *
     * @param descriptor the plugin descriptor
     * @return a {@link PluginTransitionResult} indicating success or failure
     * @throws NullPointerException if descriptor is null
     */
    public PluginTransitionResult load(PluginDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor must not be null");
        return executeLifecycleTransition(descriptor, PluginState.UNLOADED, lifecycleEngine::load);
    }

    /**
     * Starts a plugin, transitioning it from LOADED to STARTED
     * (or STOPPED to STARTED for restart scenarios).
     *
     * <p>The service delegates the transition decision to the
     * {@link PluginLifecycleEngine} and updates the stored descriptor
     * state upon success.</p>
     *
     * @param descriptor the plugin descriptor
     * @return a {@link PluginTransitionResult} indicating success or failure
     * @throws NullPointerException if descriptor is null
     */
    public PluginTransitionResult start(PluginDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor must not be null");
        PluginState current = descriptor.state();
        return switch (current) {
            case LOADED -> executeLifecycleTransition(descriptor, PluginState.LOADED, lifecycleEngine::start);
            case STOPPED -> executeLifecycleTransition(descriptor, PluginState.STOPPED, lifecycleEngine::start);
            default -> PluginTransitionResult.failure(
                    descriptor,
                    current,
                    current,
                    String.format(
                            "Cannot start plugin '%s' from state %s",
                            descriptor.plugin().id(), current
                    )
            );
        };
    }

    /**
     * Stops a plugin, transitioning it from STARTED to STOPPED.
     *
     * <p>The service delegates the transition decision to the
     * {@link PluginLifecycleEngine} and updates the stored descriptor
     * state upon success.</p>
     *
     * @param descriptor the plugin descriptor
     * @return a {@link PluginTransitionResult} indicating success or failure
     * @throws NullPointerException if descriptor is null
     */
    public PluginTransitionResult stop(PluginDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor must not be null");
        return executeLifecycleTransition(descriptor, PluginState.STARTED, lifecycleEngine::stop);
    }

    /**
     * Unloads a plugin, transitioning it from STOPPED to UNLOADED.
     *
     * <p>The service delegates the transition decision to the
     * {@link PluginLifecycleEngine} and updates the stored descriptor
     * state upon success.</p>
     *
     * @param descriptor the plugin descriptor
     * @return a {@link PluginTransitionResult} indicating success or failure
     * @throws NullPointerException if descriptor is null
     */
    public PluginTransitionResult unload(PluginDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor must not be null");
        return executeLifecycleTransition(descriptor, PluginState.STOPPED, lifecycleEngine::unload);
    }

    /**
     * Verifies the plugin's current state matches expectations, delegates to
     * the engine for transition validation, and updates the stored descriptor
     * on success.
     */
    private PluginTransitionResult executeLifecycleTransition(
            PluginDescriptor descriptor,
            PluginState expectedState,
            LifecycleTransition transition
    ) {
        PluginState current = descriptor.state();
        if (current != expectedState) {
            return PluginTransitionResult.failure(
                    descriptor,
                    current,
                    current,
                    String.format(
                            "Cannot transition plugin '%s' from %s; expected state is %s",
                            descriptor.plugin().id(), current, expectedState
                    )
            );
        }

        PluginTransitionResult engineResult = transition.apply(descriptor);
        if (engineResult.success()) {
            PluginState targetState = engineResult.currentState();
            PluginDescriptor updatedDescriptor = new PluginDescriptor(
                    descriptor.plugin(),
                    targetState,
                    descriptor.loadedAt(),
                    descriptor.provider()
            );
            plugins.put(descriptor.plugin().id(), updatedDescriptor);
            return PluginTransitionResult.success(descriptor, current, targetState);
        }
        return engineResult;
    }

    /**
     * Functional interface for engine transition methods.
     */
    @FunctionalInterface
    private interface LifecycleTransition {
        PluginTransitionResult apply(PluginDescriptor descriptor);
    }
}
