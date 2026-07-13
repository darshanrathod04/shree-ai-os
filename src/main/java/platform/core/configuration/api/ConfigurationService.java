package platform.core.configuration.api;

import platform.core.configuration.model.ConfigurationEntry;
import platform.core.configuration.model.ConfigurationKey;
import platform.core.configuration.model.ConfigurationNamespace;
import platform.core.configuration.model.ConfigurationType;

import java.util.Collection;
import java.util.Optional;

/**
 * <b>ConfigurationService</b>
 *
 * <p>The public contract for Platform configuration management within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the Platform contract for configuration operations.</li>
 *   <li>Specifies WHAT the Platform can do — implementations define HOW.</li>
 *   <li>Ensures configuration management is independent of implementation details.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Configuration Principle:</b> The Configuration API defines WHAT the Platform can do.
 * Future services define HOW the Platform does it.</p>
 *
 * @see platform.core.configuration.api package-info
 */
public interface ConfigurationService {

    /**
     * Registers a configuration entry.
     *
     * <p>If a configuration with the same key already exists, it SHALL be replaced.</p>
     *
     * @param configuration the configuration entry to register (must not be null)
     * @return {@code true} if registration succeeded, {@code false} otherwise
     * @throws IllegalArgumentException if {@code configuration} is {@code null}
     */
    boolean register(ConfigurationEntry configuration);

    /**
     * Returns the configuration entry for the given key.
     *
     * @param key the configuration key (must not be null)
     * @return an {@code Optional} containing the configuration entry if found,
     *         or an empty {@code Optional} if not found
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    Optional<ConfigurationEntry> get(ConfigurationKey key);

    /**
     * Returns all registered configuration entries.
     *
     * <p>The returned collection is a snapshot of the configurations at the time of the call.
     * It SHALL be unmodifiable.</p>
     *
     * @return an unmodifiable collection of all registered configuration entries;
     *         returns an empty collection if no configurations are registered
     */
    Collection<ConfigurationEntry> list();

    /**
     * Returns whether a configuration exists for the given key.
     *
     * @param key the configuration key (must not be null)
     * @return {@code true} if a configuration exists for the key, {@code false} otherwise
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    boolean exists(ConfigurationKey key);

    /**
     * Removes the configuration entry for the given key.
     *
     * <p>If no configuration exists for the key, this method SHALL return {@code false}.</p>
     *
     * @param key the configuration key (must not be null)
     * @return {@code true} if a configuration was removed, {@code false} otherwise
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    boolean remove(ConfigurationKey key);
}