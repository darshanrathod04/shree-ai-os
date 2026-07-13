/**
 * <b>Event Domain Models</b>
 *
 * <p>Domain models defining the Platform language for the Event Bus subsystem
 * within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the immutable domain objects used by the Event Bus subsystem.</li>
 *   <li>Provides the type-safe language that the {@code platform.core.eventbus.api}
 *       package uses for event operations.</li>
 *   <li>Ensures all event information is validated at construction time.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.eventbus.model
 * ├── Event.java             — Platform event
 * ├── EventId.java           — Unique event identity
 * ├── EventTopic.java        — Event topic
 * ├── EventSubscriber.java   — Event subscriber interface
 * ├── EventMetadata.java     — Event metadata
 * └── EventPriority.java     — Event priority enum
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>All models are immutable.</li>
 *   <li>No business logic — models are pure data carriers.</li>
 *   <li>No Spring annotations — framework-agnostic.</li>
 *   <li>No persistence annotations.</li>
 *   <li>No Lombok.</li>
 * </ul>
 *
 * @see platform.core.eventbus.model.Event
 * @see platform.core.eventbus.model.EventTopic
 * @see platform.core.eventbus.model.EventSubscriber
 */
package platform.core.eventbus.model;