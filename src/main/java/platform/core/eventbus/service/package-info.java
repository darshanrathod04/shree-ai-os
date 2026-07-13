/**
 * <b>Event Bus Service Layer</b>
 *
 * <p>Provides the default in-memory implementation of the EventBus contract
 * within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Coordinates publishing and subscriber management.</li>
 *   <li>Owns the subscriber registry.</li>
 *   <li>Delegates actual event delivery to the Event Dispatch Engine.</li>
 *   <li>Ensures all events are validated before processing.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.eventbus.service
 * ├── DefaultEventBusService.java  — Default EventBus implementation
 * └── README.md
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Thread-safe — uses ConcurrentHashMap for all storage.</li>
 *   <li>Never bypasses EventValidator — all events validated before processing.</li>
 *   <li>Never dispatches directly — delegates to EventDispatchEngine.</li>
 *   <li>Never exposes mutable collections — returns unmodifiable views.</li>
 *   <li>Never creates threads, sleeps, retries, or caches events.</li>
 * </ul>
 *
 * <p><b>Engineering Principle:</b> Service coordinates. Engine executes. Validator protects.
 * Responsibilities SHALL remain separated.</p>
 *
 * @see platform.core.eventbus.service.DefaultEventBusService
 */
package platform.core.eventbus.service;