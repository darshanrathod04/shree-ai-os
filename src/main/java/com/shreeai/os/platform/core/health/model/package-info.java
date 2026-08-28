/**
 * <b>Health Domain Models</b>
 *
 * <p>Defines the immutable Platform Language for the Health Service within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the domain models for health monitoring.</li>
 *   <li>Provides immutable value objects for type-safe health representation.</li>
 *   <li>Enables consistent health checking across Platform components.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.health.model
 * ├── HealthComponentId.java    — Immutable health component identifier
 * ├── HealthComponent.java      — Immutable health component
 * ├── HealthStatus.java         — Enum of health states
 * ├── HealthSeverity.java       — Enum of severity levels
 * ├── HealthIndicator.java      — Immutable health observation
 * ├── HealthMetrics.java        — Immutable health metrics
 * ├── HealthCheck.java          — Immutable health check request
 * ├── HealthReport.java         — Immutable health check result
 * ├── package-info.java
 * └── README.md
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Immutable — all models are immutable value objects.</li>
 *   <li>No setters — all fields are final and set via constructor.</li>
 *   <li>No Lombok — explicit constructors, getters, equals, hashCode, toString.</li>
 *   <li>No service logic — models are pure data carriers.</li>
 *   <li>No validation logic — validation belongs to the service layer.</li>
 *   <li>No persistence — no ORM annotations.</li>
 *   <li>No Spring — framework-agnostic.</li>
 *   <li>No Records — uses explicit classes for compatibility.</li>
 * </ul>
 *
 * <p><b>Engineering Principle:</b> Platform Language belongs inside the model package.
 * API packages expose contracts only.</p>
 *
 * @see com.shreeai.os.platform.core.health.model.HealthComponentId
 * @see com.shreeai.os.platform.core.health.model.HealthComponent
 * @see com.shreeai.os.platform.core.health.model.HealthStatus
 * @see com.shreeai.os.platform.core.health.model.HealthSeverity
 * @see com.shreeai.os.platform.core.health.model.HealthIndicator
 * @see com.shreeai.os.platform.core.health.model.HealthMetrics
 * @see com.shreeai.os.platform.core.health.model.HealthCheck
 * @see com.shreeai.os.platform.core.health.model.HealthReport
 */
package com.shreeai.os.platform.core.health.model;