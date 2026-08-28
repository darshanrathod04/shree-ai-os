/**
 * <b>Plugin Error</b>
 *
 * <p>Error handling architecture for the Plugin Framework within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines error codes for all Plugin subsystem failures.</li>
 *   <li>Provides immutable error model with structured data.</li>
 *   <li>Enables consistent exception handling via base {@link PluginException}.</li>
 *   <li>Never executes plugin operations or loads plugins.</li>
 * </ul>
 *
 * <p><b>Package Boundaries:</b></p>
 * <ul>
 *   <li>This package contains ONLY error definitions.</li>
 *   <li>No business logic.</li>
 *   <li>No persistence.</li>
 *   <li>No services.</li>
 *   <li>No plugin loading.</li>
 *   <li>No lifecycle management.</li>
 *   <li>No events.</li>
 *   <li>No threading.</li>
 * </ul>
 *
 * <p><b>Error Hierarchy:</b></p>
 * <ul>
 *   <li>{@link PluginErrorCode} — Enumeration of error conditions</li>
 *   <li>{@link PluginError} — Immutable error model</li>
 *   <li>{@link PluginException} — Base runtime exception</li>
 *   <li>{@link DuplicatePluginException} — Duplicate registration</li>
 *   <li>{@link PluginNotFoundException} — Plugin not found</li>
 *   <li>{@link InvalidPluginException} — Validation failure</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * @see PluginErrorCode
 * @see PluginError
 * @see PluginException
 */
package com.shreeai.os.platform.core.plugin.error;