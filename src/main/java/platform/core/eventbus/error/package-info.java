/**
 * <b>Event Bus Error Architecture</b>
 *
 * <p>Defines all standard errors used by the Event Bus subsystem
 * within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a complete error architecture for the Event Bus subsystem.</li>
 *   <li>Ensures consistent error reporting across all Event Bus operations.</li>
 *   <li>Follows the Platform-wide error pattern established by other subsystems.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.eventbus.error
 * ├── EventErrorCode.java           — Error code enum
 * ├── EventError.java               — Immutable error model
 * ├── EventBusException.java        — Base runtime exception
 * ├── InvalidEventException.java    — Validation failure
 * ├── NoSubscribersException.java   — No subscribers found
 * ├── EventDispatchException.java   — Dispatch failure
 * └── README.md
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>All error models are immutable.</li>
 *   <li>No business logic — errors are pure data carriers.</li>
 *   <li>No Spring annotations — framework-agnostic.</li>
 *   <li>No persistence annotations.</li>
 *   <li>No Lombok.</li>
 * </ul>
 *
 * <p><b>Error Principle:</b> Every Platform Core Service owns its own Error Architecture.
 * All Error Architectures SHALL follow one Platform pattern.</p>
 *
 * @see platform.core.eventbus.error.EventErrorCode
 * @see platform.core.eventbus.error.EventError
 * @see platform.core.eventbus.error.EventBusException
 */
package platform.core.eventbus.error;