package platform.core.configuration.service;

import platform.core.configuration.api.ConfigurationService;
import platform.core.configuration.engine.ConfigurationResolutionEngine;
import platform.core.configuration.engine.ResolutionResult;
import platform.core.configuration.error.ConfigurationError;
import platform.core.configuration.error.ConfigurationErrorCode;
import platform.core.configuration.error.DuplicateConfigurationException;
import platform.core.configuration.error.InvalidConfigurationException;
import platform.core.configuration.model.ConfigurationEntry;
import platform.core.configuration.model.ConfigurationKey;
import platform.core.configuration.validator.ConfigurationValidator;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>DefaultConfigurationService</b>
 *
 * <p>The default in-memory implementation of the {@link ConfigurationService} contract
 * within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Implements the ConfigurationService contract.</li>
 *   <li>Owns the configuration storage.</li>
 *   <li>Coordinates validation and error handling.</li>
 *   <li>Ensures thread-safe configuration management.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Engineering Principle:</b> ConfigurationService owns storage. Validator owns validation.
 * Errors own failure reporting. Responsibilities SHALL remain independent forever.</p>
 *
 * @see ConfigurationService
 * @see ConfigurationValidator
 */
public final class DefaultConfigurationService implements ConfigurationService {

    private final ConfigurationValidator validator;
    private final ConfigurationResolutionEngine engine;
    private final Map<ConfigurationKey, ConfigurationEntry> storage;

    /**
     * Constructs a new {@code DefaultConfigurationService} with default validator and engine.
     */
    public DefaultConfigurationService() {
        this.validator = new ConfigurationValidator();
        this.engine = new ConfigurationResolutionEngine();
        this.storage = new ConcurrentHashMap<>();
    }

    /**
     * Constructs a new {@code DefaultConfigurationService} with the given validator and engine.
     *
     * @param validator the configuration validator (must not be null)
     * @param engine the configuration resolution engine (must not be null)
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public DefaultConfigurationService(ConfigurationValidator validator, ConfigurationResolutionEngine engine) {
        this.validator = Objects.requireNonNull(validator, "ConfigurationValidator must not be null");
        this.engine = Objects.requireNonNull(engine, "ConfigurationResolutionEngine must not be null");
        this.storage = new ConcurrentHashMap<>();
    }

    /**
     * Registers a configuration entry.
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Validate entry</li>
     *   <li>Reject duplicate keys</li>
     *   <li>Store entry</li>
     *   <li>Return true</li>
     * </ol>
     *
     * @param configuration the configuration entry to register (must not be null)
     * @return {@code true} if registration succeeded
     * @throws IllegalArgumentException if {@code configuration} is {@code null}
     * @throws DuplicateConfigurationException if a configuration with the same key already exists
     * @throws InvalidConfigurationException if the configuration is invalid
     */
    @Override
    public boolean register(ConfigurationEntry configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("ConfigurationEntry must not be null");
        }

        // Step 1: Validate entry
        var validationResult = validator.validateEntry(configuration);
        if (!validationResult.isValid()) {
            throw new InvalidConfigurationException(
                    new ConfigurationError(
                            ConfigurationErrorCode.CONFIGURATION_VALIDATION_FAILED,
                            "Configuration validation failed: " + validationResult.errors(),
                            Instant.now(),
                            Map.of()
                    )
            );
        }

        // Step 2: Reject duplicate keys
        ConfigurationKey key = configuration.key();
        if (storage.containsKey(key)) {
            throw new DuplicateConfigurationException(
                    new ConfigurationError(
                            ConfigurationErrorCode.CONFIGURATION_DUPLICATE,
                            "Configuration with key '" + key.value() + "' already exists",
                            Instant.now(),
                            Map.of("key", key.value())
                    )
            );
        }

        // Step 3: Store entry
        storage.put(key, configuration);

        // Step 4: Return true
        return true;
    }

    /**
     * Returns the configuration entry for the given key.
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Lookup map</li>
     *   <li>Delegate to engine for resolution</li>
     *   <li>Return Optional</li>
     * </ol>
     *
     * @param key the configuration key (must not be null)
     * @return an {@code Optional} containing the configuration entry if found,
     *         or an empty {@code Optional} if not found
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    @Override
    public Optional<ConfigurationEntry> get(ConfigurationKey key) {
        if (key == null) {
            throw new IllegalArgumentException("ConfigurationKey must not be null");
        }

        // Step 1: Lookup map
        ConfigurationEntry entry = storage.get(key);

        // Step 2: Delegate to engine for resolution
        if (entry != null) {
            ResolutionResult result = engine.resolve(key, entry);
            if (!result.success()) {
                return Optional.empty();
            }
        }

        // Step 3: Return Optional
        return Optional.ofNullable(entry);
    }

    /**
     * Returns all registered configuration entries.
     *
     * <p>The returned collection is a snapshot of the configurations at the time of the call.
     * It SHALL be unmodifiable.</p>
     *
     * @return an unmodifiable collection of all registered configuration entries;
     *         returns an empty collection if no configurations are registered
     */
    @Override
    public Collection<ConfigurationEntry> list() {
        return Collections.unmodifiableCollection(storage.values());
    }

    /**
     * Returns whether a configuration exists for the given key.
     *
     * <p>This is a lookup-only operation — no validation is performed.</p>
     *
     * @param key the configuration key (must not be null)
     * @return {@code true} if a configuration exists for the key, {@code false} otherwise
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    @Override
    public boolean exists(ConfigurationKey key) {
        if (key == null) {
            throw new IllegalArgumentException("ConfigurationKey must not be null");
        }

        // Lookup only
        return storage.containsKey(key);
    }

    /**
     * Removes the configuration entry for the given key.
     *
     * <p>Flow:</p>
     * <ol>
     *   <li>Validate key</li>
     *   <li>Reject read-only entries</li>
     *   <li>Remove</li>
     *   <li>Return result</li>
     * </ol>
     *
     * @param key the configuration key (must not be null)
     * @return {@code true} if a configuration was removed, {@code false} otherwise
     * @throws IllegalArgumentException if {@code key} is {@code null}
     * @throws InvalidConfigurationException if the configuration is read-only
     */
    @Override
    public boolean remove(ConfigurationKey key) {
        if (key == null) {
            throw new IllegalArgumentException("ConfigurationKey must not be null");
        }

        // Step 1: Validate key
        var validationResult = validator.validateKey(key);
        if (!validationResult.isValid()) {
            return false;
        }

        // Step 2: Check if entry exists and is read-only
        ConfigurationEntry entry = storage.get(key);
        if (entry != null && entry.readOnly()) {
            throw new InvalidConfigurationException(
                    new ConfigurationError(
                            ConfigurationErrorCode.CONFIGURATION_READ_ONLY,
                            "Configuration with key '" + key.value() + "' is read-only and cannot be removed",
                            Instant.now(),
                            Map.of("key", key.value())
                    )
            );
        }

        // Step 3: Remove
        ConfigurationEntry removed = storage.remove(key);

        // Step 4: Return result
        return removed != null;
    }
}