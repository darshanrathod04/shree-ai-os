/**
 * <b>Runtime Kernel</b>
 *
 * <p>The Runtime Kernel is the execution foundation of Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the execution environment for all platform operations.</li>
 *   <li>Manages Runtime lifecycle, state transitions, and execution sessions.</li>
 *   <li>Enforces Runtime contracts on all execution requests.</li>
 *   <li>Maintains clear boundaries from other kernels (Memory, Planning, Cognitive).</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.runtime
 * ├── api/          — Public Runtime API (Runtime, RuntimeBuilder)
 * ├── config/       — Runtime configuration
 * ├── contracts/    — Runtime contracts
 * ├── execution/    — Execution model (request, session, context, pipeline, result)
 * ├── lifecycle/    — Runtime lifecycle and state management
 * ├── exceptions/   — Runtime exception hierarchy
 * └── internal/     — Internal implementation (not part of public API)
 * </pre>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 * <p><b>Constitutional Authority:</b> CONST-001</p>
 */
package com.shreeai.os.platform.runtime;