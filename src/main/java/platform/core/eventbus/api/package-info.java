/**
 * <b>Event Bus Public API</b>
 *
 * <p>Defines how Platform components publish and subscribe to events
 * within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the public contract for Platform event communication.</li>
 *   <li>Specifies WHAT the Event Bus can do — implementations define HOW.</li>
 *   <li>Enables decoupled communication between Platform components.</li>
 *   <li>Ensures components communicate through contracts, not direct dependencies.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.eventbus.api
 * └── EventBus.java  — Public event bus contract
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Out of Scope:</b></p>
 * <ul>
 *   <li>Models — {@code Event}, {@code EventTopic}, {@code EventSubscriber} are defined in EIO-402.</li>
 *   <li>Implementation — no implementation classes in this package.</li>
 *   <li>Storage — event storage belongs in the implementation.</li>
 *   <li>Dispatching — event routing belongs in the implementation.</li>
 *   <li>Threading — threading model belongs in the implementation.</li>
 * </ul>
 *
 * @see platform.core.eventbus.api.EventBus
 */
package platform.core.eventbus.api;