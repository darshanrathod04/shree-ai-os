/**
 * <b>Planning Domain Model</b>
 *
 * <p>This package contains the canonical immutable domain model for the Planning Kernel.
 * These value objects represent planning concepts throughout the platform and serve as
 * the foundational layer upon which the Planning API and all future Planning Kernel
 * implementations are built.</p>
 *
 * <p><b>Immutable Design Philosophy:</b></p>
 * <ul>
 *   <li>All models are immutable — once constructed, their state cannot change.</li>
 *   <li>Constructor validation ensures all required fields are non-null.</li>
 *   <li>Defensive copying protects mutable collection inputs.</li>
 *   <li>No setters are exposed — state is provided only at construction time.</li>
 *   <li>Value-based equality is implemented via {@code equals()}, {@code hashCode()},
 *       and {@code toString()}.</li>
 * </ul>
 *
 * <p><b>Separation from API Contracts:</b></p>
 * <ul>
 *   <li>Domain models are distinct from API request/response types.</li>
 *   <li>API service interfaces reference these stable domain model types.</li>
 *   <li>Domain models contain no planning algorithms, scheduling logic,
 *       prioritization logic, validation behavior, or execution logic.</li>
 *   <li>Behavior belongs to future Service and Engine layers.</li>
 * </ul>
 *
 * <p><b>Kernel Development Standard Compliance:</b></p>
 * <ul>
 *   <li>Java 21 records and final classes with constructor validation.</li>
 *   <li>No framework dependencies (Spring, Lombok, JPA).</li>
 *   <li>No mutable state, setters, or business logic.</li>
 *   <li>Comprehensive JavaDocs for all public types and methods.</li>
 *   <li>Consistent with Identity, Memory, Context, Knowledge, and Cognitive kernels.</li>
 * </ul>
 *
 * <p><b>Domain Architecture:</b></p>
 * <pre>
 *                  PlanningObjective
 *                         │
 *                         ▼
 *                      Goal
 *                         │
 *               ┌─────────┴─────────┐
 *               ▼                   ▼
 *      GoalConstraints           Task
 *                                    │
 *                          ┌─────────┴─────────┐
 *                          ▼                   ▼
 *                 TaskRequirements      Priority
 *                          │
 *                      Schedule
 *                          │
 *               ┌─────────┴─────────┐
 *               ▼                   ▼
 *  SchedulingConstraints   ResourceAvailability
 *                          │
 *                          ▼
 *                 ValidationCriteria
 *                          │
 *                          ▼
 *                  PlanningSnapshot
 * </pre>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-102, EIO-ARCH-001</p>
 */
package com.shreeai.os.platform.kernels.planning.model;