/**
 * <b>Plugin Lifecycle Engine</b>
 *
 * <p>This package provides a stateless, thread-safe engine for managing
 * plugin lifecycle transitions within Shree AI OS. It defines the core
 * state machine and transition rules without any dependency on storage,
 * classloading, or event publishing.</p>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link platform.core.plugin.engine.PluginTransitionResult} — Immutable
 *       result capturing the outcome of a lifecycle transition.</li>
 *   <li>{@link platform.core.plugin.engine.PluginLifecycleEngine} — Stateless
 *       engine that validates and executes state transitions.</li>
 * </ul>
 *
 * <h2>State Diagram</h2>
 * <pre>
 *        ┌─────────────────────────────────────┐
 *        │                                     │
 *        v                                     │
 *   ┌─────────┐  load   ┌────────┐  start  ┌─────────┐
 *   │UNLOADED │───────→│ LOADED │───────→│ STARTED │
 *   └─────────┘        └────────┘        └─────────┘
 *        ↑                                    │
 *        │         ┌──────────┐               │ stop
 *        └─────────│  STOPPED │←──────────────┘
 *                  └──────────┘
 *                       │
 *                       │ start
 *                       v
 *                  ┌─────────┐
 *                  │ STARTED │
 *                  └─────────┘
 * </pre>
 *
 * <h2>Transition Table</h2>
 * <table border="1">
 *   <caption>Allowed Transitions</caption>
 *   <tr><th>From</th><th>To</th><th>Method</th></tr>
 *   <tr><td>UNLOADED</td><td>LOADED</td><td>{@code load()}</td></tr>
 *   <tr><td>LOADED</td><td>STARTED</td><td>{@code start()}</td></tr>
 *   <tr><td>STARTED</td><td>STOPPED</td><td>{@code stop()}</td></tr>
 *   <tr><td>STOPPED</td><td>STARTED</td><td>{@code start()}</td></tr>
 *   <tr><td>STOPPED</td><td>UNLOADED</td><td>{@code unload()}</td></tr>
 * </table>
 *
 * <h2>Engineering Principles</h2>
 * <ul>
 *   <li><strong>Stateless</strong> — The engine holds no mutable state.</li>
 *   <li><strong>Thread-safe</strong> — All classes are immutable or effectively immutable.</li>
 *   <li><strong>Deterministic</strong> — Given the same inputs, the engine always produces the same output.</li>
 *   <li><strong>No persistence</strong> — No database, filesystem, or I/O.</li>
 *   <li><strong>No plugin loading</strong> — Does not load JARs or classloaders.</li>
 *   <li><strong>No Spring, Lombok, or JPA</strong> — Pure Java with zero framework coupling.</li>
 * </ul>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * @see platform.core.plugin.engine.PluginLifecycleEngine
 * @see platform.core.plugin.engine.PluginTransitionResult
 * @see platform.core.plugin.model.PluginState
 * @see platform.core.plugin.model.PluginDescriptor
 */
package platform.core.plugin.engine;