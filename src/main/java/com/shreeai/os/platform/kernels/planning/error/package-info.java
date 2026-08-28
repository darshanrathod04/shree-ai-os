/**
 * <b>Planning Error Layer</b>
 *
 * <p>This package defines the Error Architecture for the Planning Kernel.
 * It standardizes how planning failures are represented and communicated
 * throughout the kernel using immutable error objects, a strongly typed error
 * code system, and a domain-specific exception hierarchy.</p>
 *
 * <p><b>Immutable Error Philosophy:</b></p>
 * <ul>
 *   <li>All error objects are immutable and thread-safe.</li>
 *   <li>Defensive copying is applied to all mutable inputs.</li>
 *   <li>No setters are exposed — state is provided only at construction time.</li>
 *   <li>Value-based equality is implemented via {@code equals()}, {@code hashCode()},
 *       and {@code toString()}.</li>
 * </ul>
 *
 * <p><b>Exception Hierarchy:</b></p>
 * <pre>
 * RuntimeException
 *     │
 *     ▼
 * PlanningException
 *     │
 *     ├── GoalPlanningException
 *     ├── TaskPlanningException
 *     ├── SchedulingException
 *     ├── PriorityException
 *     └── PlanValidationException
 * </pre>
 *
 * <p><b>Separation from Validation and Planning Logic:</b></p>
 * <ul>
 *   <li>This layer classifies failures only — it never attempts to recover from them.</li>
 *   <li>Error objects are immutable data carriers with no behavior.</li>
 *   <li>Exceptions communicate failures across Service and Engine layers.</li>
 *   <li>No retry, recovery, planning, scheduling, or prioritization logic exists.</li>
 * </ul>
 *
 * <p><b>Kernel Development Standard Compliance:</b></p>
 * <ul>
 *   <li>Java 21 with immutable value objects and final classes.</li>
 *   <li>Constructor validation and defensive copying.</li>
 *   <li>No framework dependencies (Spring, Lombok, JPA).</li>
 *   <li>No mutable state or business logic.</li>
 *   <li>Comprehensive JavaDocs for all public types and methods.</li>
 *   <li>Thread-safe design — all objects are safely shareable across threads.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-104, EIO-ARCH-001</p>
 */
package com.shreeai.os.platform.kernels.planning.error;