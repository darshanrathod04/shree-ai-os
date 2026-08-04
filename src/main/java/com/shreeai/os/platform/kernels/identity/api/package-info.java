/**
 * <b>Identity Kernel Public API</b>
 *
 * <p>Defines the stable public contracts for the Identity Kernel within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the public contract for all Identity operations.</li>
 *   <li>Specifies WHAT the Identity Kernel can do — implementations define HOW.</li>
 *   <li>Enforces that no kernel accesses Identity internals directly.</li>
 *   <li>Provides stable, framework-agnostic contracts for platform-wide communication.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.kernels.identity.api
 * ├── IdentityKernel.java      — Primary public entry point
 * ├── IdentityCommands.java    — State-modifying operations
 * ├── IdentityQueries.java     — Read-only operations
 * ├── IdentityEvents.java      — Event definitions
 * └── IdentityContract.java    — Unified contract aggregator
 * </pre>
 *
 * <p><b>Ownership:</b> Identity Kernel</p>
 * <p><b>Constitutional Authority:</b> ADD-104, ADD-105, ADD-106, KERNEL-ISO-001</p>
 *
 * <p><b>Out of Scope:</b></p>
 * <ul>
 *   <li>Models — request/result types are defined in EIO-ID-102.</li>
 *   <li>Implementation — no implementation classes in this package.</li>
 *   <li>Validation — validation logic belongs in the implementation layer.</li>
 *   <li>Storage — persistence concerns are handled by the implementation.</li>
 *   <li>Business Logic — algorithms belong in the implementation layer.</li>
 * </ul>
 *
 * @see com.shreeai.os.platform.kernels.identity.api.IdentityKernel
 * @see com.shreeai.os.platform.kernels.identity.api.IdentityCommands
 * @see com.shreeai.os.platform.kernels.identity.api.IdentityQueries
 * @see com.shreeai.os.platform.kernels.identity.api.IdentityEvents
 * @see com.shreeai.os.platform.kernels.identity.api.IdentityContract
 */
package com.shreeai.os.platform.kernels.identity.api;