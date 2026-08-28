/**
 * <b>Plugin API</b>
 *
 * <p>Public contracts for the Plugin Framework within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines WHAT the Platform can do with plugins.</li>
 *   <li>Does NOT define HOW plugins are managed.</li>
 *   <li>Provides interfaces and models for plugin registration, discovery, and lifecycle.</li>
 *   <li>Remains framework-agnostic and implementation-agnostic.</li>
 * </ul>
 *
 * <p><b>Package Boundaries:</b></p>
 * <ul>
 *   <li>This package contains ONLY public contracts.</li>
 *   <li>No implementation classes.</li>
 *   <li>No business logic.</li>
 *   <li>No persistence.</li>
 *   <li>No events.</li>
 *   <li>No threading.</li>
 *   <li>No monitoring.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * @see PluginService
 * @see Plugin
 * @see PluginDescriptor
 * @see PluginState
 * @see PluginRequest
 */
package com.shreeai.os.platform.core.plugin.api;