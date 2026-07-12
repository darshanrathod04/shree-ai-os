/**
 * <b>Kernel Registry Public API</b>
 *
 * <p>The Kernel Registry is the central registration authority for all kernels
 * within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the public contract for kernel registration and discovery.</li>
 *   <li>Enables kernels to register, unregister, and be discovered by the platform.</li>
 *   <li>Maintains a registry of known kernels and their metadata.</li>
 *   <li>Enforces the principle that no kernel participates in the platform
 *       until it is formally registered (KERNEL-005).</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.registry.api
 * └── KernelRegistry.java  — Public registry contract
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> CONST-001, KERNEL-005, ADD-PLT-202, ADD-PLT-205</p>
 *
 * <p><b>Out of Scope:</b></p>
 * <ul>
 *   <li>Implementation — this package contains only the public contract.</li>
 *   <li>Models — registration entry types are defined by the implementation.</li>
 *   <li>Validation — validation logic belongs in the implementation layer.</li>
 *   <li>Exceptions — exception types are defined by the implementation.</li>
 *   <li>Lifecycle — lifecycle management is handled by the implementation.</li>
 *   <li>Event Bus — event publishing is handled by the implementation.</li>
 * </ul>
 *
 * @see platform.core.registry.api.KernelRegistry
 */
package platform.core.registry.api;