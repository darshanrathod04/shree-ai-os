/**
 * <b>Lifecycle Public API</b>
 *
 * <p>The Lifecycle API defines the Platform contract for Kernel lifecycle management
 * within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the official Platform contract for kernel lifecycle operations.</li>
 *   <li>Specifies WHAT the Platform can do — future services define HOW.</li>
 *   <li>Ensures lifecycle management is independent of implementation details.</li>
 *   <li>Provides a stable API for kernel state and health queries.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.lifecycle.api
 * └── LifecycleService.java  — Public lifecycle contract
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> CONST-001, KERNEL-008, KERNEL-009, KERNEL-010,
 * KERNEL-011, KERNEL-012, ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Out of Scope:</b></p>
 * <ul>
 *   <li>Models — {@code KernelState} and {@code KernelHealth} are defined in EIO-302.</li>
 *   <li>Implementation — no implementation classes in this package.</li>
 *   <li>Exceptions — exception types are defined by the implementation.</li>
 *   <li>Validation — validation logic belongs in the implementation layer.</li>
 *   <li>Tests — testing is handled by the implementation.</li>
 * </ul>
 *
 * @see com.shreeai.os.platform.core.lifecycle.api.LifecycleService
 */
package com.shreeai.os.platform.core.lifecycle.api;