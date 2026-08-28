/**
 * <b>Lifecycle Error Architecture</b>
 *
 * <p>Standardized error model for the Lifecycle Service within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the standard error model used by all Lifecycle services.</li>
 *   <li>Provides a consistent error architecture following the Platform pattern.</li>
 *   <li>Ensures all lifecycle errors are consistent, typed, and documented.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.lifecycle.error
 * ├── LifecycleErrorCode.java           — Standardized error codes
 * ├── LifecycleError.java               — Immutable error description
 * ├── LifecycleException.java           — Base exception
 * ├── InvalidTransitionException.java   — Invalid state transition
 * ├── KernelNotInitializedException.java — Kernel not initialized
 * └── KernelAlreadyRunningException.java — Kernel already running
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> CONST-001, KERNEL-008, KERNEL-009, KERNEL-010,
 * KERNEL-011, KERNEL-012, ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>LifecycleException is the ONLY base exception — all future exceptions extend it.</li>
 *   <li>Immutable where applicable — LifecycleError is immutable.</li>
 *   <li>No business logic — error definitions only.</li>
 *   <li>No Spring annotations — framework-agnostic.</li>
 *   <li>No persistence — no database or serialization annotations.</li>
 * </ul>
 *
 * @see com.shreeai.os.platform.core.lifecycle.error.LifecycleException
 * @see com.shreeai.os.platform.core.lifecycle.error.LifecycleError
 * @see com.shreeai.os.platform.core.lifecycle.error.LifecycleErrorCode
 */
package com.shreeai.os.platform.core.lifecycle.error;