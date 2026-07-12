/**
 * <b>Discovery Domain Models</b>
 *
 * <p>Domain models defining the Platform language for capability discovery within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the immutable value types that represent capability identity, contract identity,
 *       resolution status, and discovery results.</li>
 *   <li>Provides the type-safe language that the {@code platform.core.discovery.api}
 *       package uses for capability resolution.</li>
 *   <li>Ensures all capability and contract information is validated at construction time.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.discovery.model
 * ├── CapabilityId.java       — Unique capability identity
 * ├── ContractId.java         — Discoverable contract identity
 * ├── ResolutionStatus.java   — Resolution result enum
 * └── DiscoveryResult.java    — Composed discovery result
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> CONST-001, KERNEL-006, ADD-PLT-202,
 * ADD-PLT-205, ADD-PLT-206</p>
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
 * @see platform.core.discovery.model.CapabilityId
 * @see platform.core.discovery.model.ContractId
 * @see platform.core.discovery.model.ResolutionStatus
 * @see platform.core.discovery.model.DiscoveryResult
 */
package platform.core.discovery.model;