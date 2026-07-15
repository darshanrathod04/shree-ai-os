/**
 * <b>Plugin Service</b>
 *
 * <p>Service implementations for the Plugin Framework within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Implements the PluginService contract.</li>
 *   <li>Owns plugin storage and coordinates operations.</li>
 *   <li>Delegates validation to PluginValidator.</li>
 *   <li>Delegates lifecycle to PluginLifecycleEngine.</li>
 * </ul>
 *
 * <p><b>Package Boundaries:</b></p>
 * <ul>
 *   <li>This package contains ONLY service implementations.</li>
 *   <li>No business logic — coordination only.</li>
 *   <li>No persistence — in-memory storage only.</li>
 *   <li>No plugin loading.</li>
 *   <li>No events.</li>
 *   <li>No threading.</li>
 * </ul>
 *
 * <p><b>Engineering Principle:</b> Service coordinates. Validator validates.
 * Engine executes. Error reports failures.
 * These responsibilities shall remain independent forever.</p>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * @see DefaultPluginService
 * @see PluginService
 * @see PluginValidator
 * @see PluginLifecycleEngine
 */
package platform.core.plugin.service;