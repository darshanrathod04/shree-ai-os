/**
 * <b>Plugin Model</b>
 *
 * <p>Immutable domain models for the Plugin Framework within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the immutable Platform Language for plugins.</li>
 *   <li>Contains only data structures — no business logic.</li>
 *   <li>Provides value objects with constructor validation.</li>
 *   <li>Ensures thread-safety through immutability.</li>
 * </ul>
 *
 * <p><b>Package Boundaries:</b></p>
 * <ul>
 *   <li>This package contains ONLY immutable models.</li>
 *   <li>No business logic.</li>
 *   <li>No persistence.</li>
 *   <li>No services.</li>
 *   <li>No validation.</li>
 *   <li>No events.</li>
 *   <li>No threading.</li>
 *   <li>No monitoring.</li>
 * </ul>
 *
 * <p><b>Immutability Rules:</b></p>
 * <ul>
 *   <li>All classes are final — cannot be extended.</li>
 *   <li>All fields are final — set once in constructor.</li>
 *   <li>No setters — state cannot be modified after construction.</li>
 *   <li>Constructor validation — null/blank checks on all inputs.</li>
 *   <li>Value equality — equals() and hashCode() based on field values.</li>
 *   <li>Defensive copies — mutable fields are copied in constructors.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301, STD-003</p>
 *
 * @see PluginId
 * @see Plugin
 * @see PluginDescriptor
 * @see PluginState
 * @see PluginRequest
 */
package platform.core.plugin.model;