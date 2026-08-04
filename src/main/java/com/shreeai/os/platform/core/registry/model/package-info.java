/**
 * <b>Kernel Registry Domain Models</b>
 *
 * <p>Domain models defining the Platform language used by the Kernel Registry API.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the immutable value types that represent kernel identity, version,
 *       metadata, and registration state.</li>
 *   <li>Provides the type-safe language that the {@code platform.core.registry.api}
 *       package uses for registration and discovery.</li>
 *   <li>Ensures all kernel identity and version information is validated at
 *       construction time.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.registry.model
 * ├── KernelId.java           — Unique kernel identity
 * ├── KernelVersion.java      — Semantic version (Major.Minor.Patch)
 * ├── KernelMetadata.java     — Descriptive kernel information
 * └── RegisteredKernel.java   — Composed registration record
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> CONST-001, KERNEL-005, KERNEL-006,
 * ADD-PLT-202, ADD-PLT-205</p>
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
 * @see com.shreeai.os.platform.core.registry.model.KernelId
 * @see com.shreeai.os.platform.core.registry.model.KernelVersion
 * @see com.shreeai.os.platform.core.registry.model.KernelMetadata
 * @see com.shreeai.os.platform.core.registry.model.RegisteredKernel
 */
package com.shreeai.os.platform.core.registry.model;