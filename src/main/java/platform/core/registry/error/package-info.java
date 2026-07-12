/**
 * <b>Registry Error Architecture</b>
 *
 * <p>Standardized error model for the Kernel Registry within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a standardized error model for the Kernel Registry.</li>
 *   <li>Defines error codes, structured error descriptions, and a base exception hierarchy.</li>
 *   <li>Supports future Platform Core Services without redesign.</li>
 *   <li>Ensures all registry errors are consistent, typed, and documented.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.registry.error
 * ├── RegistryErrorCode.java       — Standardized error codes
 * ├── RegistryError.java           — Immutable error description
 * ├── RegistryException.java       — Base exception
 * ├── DuplicateKernelException.java
 * ├── InvalidKernelException.java
 * └── KernelNotFoundException.java
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> CONST-001, KERNEL-005, KERNEL-007,
 * ADD-PLT-202, ADD-PLT-205</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>All error types are immutable where applicable.</li>
 *   <li>RegistryException is the ONLY base exception — all future exceptions extend it.</li>
 *   <li>No business logic — error definitions only.</li>
 *   <li>No Spring annotations — framework-agnostic.</li>
 * </ul>
 *
 * @see platform.core.registry.error.RegistryErrorCode
 * @see platform.core.registry.error.RegistryError
 * @see platform.core.registry.error.RegistryException
 */
package platform.core.registry.error;