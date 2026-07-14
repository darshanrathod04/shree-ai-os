package platform.core.plugin.api;

import java.util.Collection;
import java.util.Optional;

/**
 * <b>PluginService</b>
 *
 * <p>Public API for the Plugin Framework within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines WHAT the Platform can do with plugins.</li>
 *   <li>Does NOT define HOW plugins are managed.</li>
 *   <li>Provides contracts for plugin registration, discovery, and lifecycle.</li>
 *   <li>Remains framework-agnostic and implementation-agnostic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Interface only — no implementation.</li>
 *   <li>No business logic.</li>
 *   <li>No persistence.</li>
 *   <li>No events.</li>
 *   <li>No threading.</li>
 *   <li>No monitoring.</li>
 *   <li>Framework agnostic.</li>
 * </ul>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * @see Plugin
 * @see PluginDescriptor
 * @see PluginState
 * @see PluginRequest
 */
public interface PluginService {

    /**
     * Register a plugin with the framework.
     *
     * <p>Registration makes the plugin available for discovery and management.</p>
     *
     * <p><b>Contract:</b></p>
     * <ul>
     *   <li>Returns true if registration succeeds.</li>
     *   <li>Returns false if plugin is already registered.</li>
     *   <li>Never throws exceptions for duplicate registration.</li>
     * </ul>
     *
     * @param plugin the plugin to register
     * @return true if registered, false if already exists
     * @throws IllegalArgumentException if plugin is null
     */
    boolean register(Plugin plugin);

    /**
     * Retrieve plugin descriptor by plugin reference.
     *
     * <p><b>Contract:</b></p>
     * <ul>
     *   <li>Returns Optional containing descriptor if plugin exists.</li>
     *   <li>Returns empty Optional if plugin not found.</li>
     * </ul>
     *
     * @param plugin the plugin to retrieve
     * @return Optional containing PluginDescriptor, or empty if not found
     * @throws IllegalArgumentException if plugin is null
     */
    Optional<PluginDescriptor> get(Plugin plugin);

    /**
     * List all registered plugins.
     *
     * <p><b>Contract:</b></p>
     * <ul>
     *   <li>Returns unmodifiable collection of all registered plugin descriptors.</li>
     *   <li>Returns empty collection if no plugins registered.</li>
     *   <li>Never returns null.</li>
     * </ul>
     *
     * @return unmodifiable collection of PluginDescriptor instances
     */
    Collection<PluginDescriptor> list();

    /**
     * Unregister a plugin from the framework.
     *
     * <p><b>Contract:</b></p>
     * <ul>
     *   <li>Returns true if unregistration succeeds.</li>
     *   <li>Returns false if plugin not found.</li>
     *   <li>Never throws exceptions for non-existent plugins.</li>
     * </ul>
     *
     * @param plugin the plugin to unregister
     * @return true if unregistered, false if not found
     * @throws IllegalArgumentException if plugin is null
     */
    boolean unregister(Plugin plugin);

    /**
     * Check if a plugin is registered.
     *
     * <p><b>Contract:</b></p>
     * <ul>
     *   <li>Returns true if plugin is registered.</li>
     *   <li>Returns false if plugin not found.</li>
     * </ul>
     *
     * @param plugin the plugin to check
     * @return true if registered, false otherwise
     * @throws IllegalArgumentException if plugin is null
     */
    boolean exists(Plugin plugin);
}