/**
 * <b>Context Model Package</b>
 *
 * <p>This package contains the immutable domain model for the Context Kernel.</p>
 *
 * <p><b>Runtime Model Philosophy:</b></p>
 * <ul>
 *   <li>Context represents live runtime situational awareness.</li>
 *   <li>Context is temporary, lightweight, and mutable as a concept through replacement.</li>
 *   <li>Context does not represent persistent memory.</li>
 *   <li>Context does not represent identity.</li>
 * </ul>
 *
 * <p><b>Architectural Boundaries:</b></p>
 * <ul>
 *   <li>Context is runtime-only - no persistence responsibilities.</li>
 *   <li>Context is not Memory - no historical or semantic search capabilities.</li>
 *   <li>Context is not Identity - no authentication or authorization responsibilities.</li>
 *   <li>Context provides situational awareness for current execution state.</li>
 </ul>
 *
 * <p><b>Model Hierarchy:</b></p>
 * <pre>
 * Context (base)
 *   ├── ConversationContext
 *   ├── ExecutionContext
 *   │     └── TaskContext
 *   └── SessionContext
 * </pre>
 *
 * <p><b>Kernel Standard Compliance:</b></p>
 * <ul>
 *   <li>All models are immutable with final fields.</li>
 *   <li>All models implement constructor validation.</li>
 *   <li>All models implement defensive copying for collections.</li>
 *   <li>All models use ContextId for identifiers (no primitives).</li>
 *   <li>All models are Java 21 compliant.</li>
 *   <li>All models provide comprehensive JavaDocs.</li>
 * </ul>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-101, EIO-CTX-102</p>
 *
 * @since 1.0
 */
package platform.kernels.context.model;