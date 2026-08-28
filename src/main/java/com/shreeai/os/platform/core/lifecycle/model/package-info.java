/**
 * <b>Lifecycle Domain Models</b>
 *
 * <p>Domain models defining the Platform language for kernel lifecycle management
 * within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the immutable domain objects used by the Lifecycle subsystem.</li>
 *   <li>Provides the type-safe language that the {@code platform.core.lifecycle.api}
 *       package uses for lifecycle operations.</li>
 *   <li>Ensures all lifecycle information is validated at construction time.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.lifecycle.model
 * ├── KernelState.java           — Kernel lifecycle state enum
 * ├── KernelHealth.java          — Kernel health status
 * ├── LifecycleTransition.java   — State transition record
 * └── TransitionResult.java      — Transition result
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> CONST-001, KERNEL-008, KERNEL-009, KERNEL-010,
 * KERNEL-011, KERNEL-012, ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
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
 * @see com.shreeai.os.platform.core.lifecycle.model.KernelState
 * @see com.shreeai.os.platform.core.lifecycle.model.KernelHealth
 * @see com.shreeai.os.platform.core.lifecycle.model.LifecycleTransition
 * @see com.shreeai.os.platform.core.lifecycle.model.TransitionResult
 */
package com.shreeai.os.platform.core.lifecycle.model;