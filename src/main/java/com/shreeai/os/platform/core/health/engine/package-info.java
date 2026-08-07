/**
 * <b>Health Evaluation Engine</b>
 *
 * <p>Provides health evaluation capabilities for the Health subsystem within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Evaluates the health of platform components.</li>
 *   <li>Generates HealthReport with status, indicators, and metrics.</li>
 *   <li>Never validates — validation belongs to HealthValidator.</li>
 *   <li>Never coordinates — coordination belongs to HealthService.</li>
 *   <li>Never stores components — storage belongs to HealthService.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.health.engine
 * ├── HealthEvaluationEngine.java    — Stateless evaluation engine
 * ├── EvaluationResult.java          — Immutable evaluation result
 * ├── package-info.java
 * └── README.md
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Stateless — no instance fields, all state passed as parameters.</li>
 *   <li>No storage — never owns or accesses ConcurrentHashMap.</li>
 *   <li>No validation — delegates to HealthValidator.</li>
 *   <li>No coordination — delegates to HealthService.</li>
 *   <li>No threads — never creates threads or schedules jobs.</li>
 *   <li>No Spring — framework-agnostic.</li>
 *   <li>No persistence — no ORM annotations.</li>
 *   <li>No event publishing — errors are thrown, not published.</li>
 * </ul>
 *
 * <p><b>Evaluation Principle:</b> HealthEvaluationEngine evaluates health.
 * HealthValidator validates structure. HealthService coordinates everything.
 * These responsibilities shall remain independent forever.</p>
 *
 * @see com.shreeai.os.platform.core.health.engine.HealthEvaluationEngine
 * @see com.shreeai.os.platform.core.health.engine.EvaluationResult
 */
package com.shreeai.os.platform.core.health.engine;