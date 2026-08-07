/**
 * <b>Planning Kernel API Layer</b>
 *
 * <p>Provides the public service contracts for the Planning Kernel, defining
 * the interfaces through which the remainder of Shree AI OS interacts with
 * planning capabilities.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Exposes high-level planning operations through PlanningService.</li>
 *   <li>Defines goal planning contracts through GoalPlanningService.</li>
 *   <li>Defines task planning contracts through TaskPlanningService.</li>
 *   <li>Defines scheduling contracts through SchedulingService.</li>
 *   <li>Defines prioritization contracts through PrioritizationService.</li>
 *   <li>Defines plan validation contracts through PlanValidationService.</li>
 * </ul>
 *
 * <p><b>API Architecture:</b></p>
 * <pre>
 *                     PlanningService
 *                   /    |     |      \
 *                  /     |     |       \
 *                 /      |     |        \
 *     GoalPlanning  TaskPlanning  Scheduling
 *            \            |            /
 *             \           |           /
 *              \   Prioritization  /
 *                      |
 *                      |
 *             PlanValidationService
 * </pre>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only planning contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 *   <li>No default methods — pure interface contracts.</li>
 * </ul>
 *
 * <p><b>Kernel Mission:</b></p>
 * <p>The Planning Kernel is responsible for transforming cognitive intent into
 * structured plans. It provides contracts for goal decomposition, task generation,
 * dependency analysis, scheduling, prioritization, and plan validation.</p>
 *
 * <p><b>Separation of Responsibilities:</b></p>
 * <ul>
 *   <li><b>Cognitive Kernel:</b> Reasoning, decision support, reflection, cognitive state.</li>
 *   <li><b>Planning Kernel:</b> Goal decomposition, task planning, dependency planning, scheduling, prioritization, plan validation.</li>
 *   <li><b>Execution Kernel (future):</b> Workflow execution, task execution, runtime operations.</li>
 *   <li><b>Chief Kernel (future):</b> Orchestration, coordination, multi-agent planning, strategic supervision.</li>
 * </ul>
 *
 * <p><b>Platform Boundaries:</b></p>
 * <p>The Planning API must never depend directly upon:</p>
 * <ul>
 *   <li>Persistence or repositories</li>
 *   <li>Networking</li>
 *   <li>Execution engines</li>
 *   <li>Orchestration components</li>
 *   <li>UI components</li>
 *   <li>Framework-specific implementations</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-101, EIO-ARCH-001</p>
 *
 * @see PlanningService
 * @see GoalPlanningService
 * @see TaskPlanningService
 * @see SchedulingService
 * @see PrioritizationService
 * @see PlanValidationService
 */
package com.shreeai.os.platform.kernels.planning.api;