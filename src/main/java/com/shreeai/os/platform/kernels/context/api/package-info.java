/**
 * <b>Context API</b>
 *
 * <p>This package provides the public API contracts for the Context Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines contracts for Context lifecycle management.</li>
 *   <li>Defines contracts for Context query operations.</li>
 *   <li>Defines contracts for Context snapshot operations.</li>
 *   <li>Defines contracts for Context lifecycle state transitions.</li>
 *   <li>Contains only interfaces - no implementations.</li>
 * </ul>
 *
 * <p><b>Context Responsibilities:</b></p>
 * <p>The Context Kernel manages only temporary runtime state:</p>
 * <ul>
 *   <li>Active conversation context</li>
 *   <li>Runtime execution context</li>
 *   <li>Current task context</li>
 *   <li>Session context</li>
 *   <li>Temporary working context</li>
 *   <li>Environmental context</li>
 *   <li>Context lifecycle</li>
 * </ul>
 *
 * <p><b>Architectural Boundaries:</b></p>
 * <p>The Context Kernel never stores long-term information. Long-term information
 * belongs to the Memory Kernel. Context is temporary, mutable, and runtime-only.</p>
 *
 * <p><b>Architecture Overview:</b></p>
 * <pre>
 *                    User
 *                      │
 *                      ▼
 *             Context Kernel
 *                      │
 *          ┌───────────┼───────────┐
 *          ▼           ▼           ▼
 *    Conversation  Execution    Session
 *      Context       Context     Context
 *          │           │           │
 *          └───────────┼───────────┘
 *                      ▼
 *               Context Snapshot
 * </pre>
 *
 * <p><b>Thread Safety:</b></p>
 * <p>All API contracts require thread-safe implementations. Multiple kernels
 * may concurrently access and modify Context data.</p>
 *
 * <p><b>Immutability:</b></p>
 * <p>All returned Context and ContextSnapshot objects must be immutable.
 * Consumers must not modify returned objects.</p>
 *
 * <p><b>Dependencies:</b></p>
 * <ul>
 *   <li>Platform Core</li>
 *   <li>Identity Kernel (read-only references)</li>
 *   <li>Memory Kernel (read-only references)</li>
 * </ul>
 *
 * <p><b>Forbidden Dependencies:</b></p>
 * <ul>
 *   <li>Planning Kernel</li>
 *   <li>Chief Kernel</li>
 *   <li>Knowledge Kernel</li>
 *   <li>Execution Kernel</li>
 *   <li>LLM integrations</li>
 *   <li>Networking</li>
 *   <li>Persistence</li>
 *   <li>UI</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-101, EIO-ARCH-001</p>
 *
 * @see com.shreeai.os.platform.kernels.context.api.ContextService
 * @see com.shreeai.os.platform.kernels.context.api.ContextQueryService
 * @see com.shreeai.os.platform.kernels.context.api.ContextSnapshotService
 * @see com.shreeai.os.platform.kernels.context.api.ContextLifecycleService
 */
package com.shreeai.os.platform.kernels.context.api;