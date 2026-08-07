/**
 * <b>Health Service Public API</b>
 *
 * <p>Defines health monitoring contracts for the Platform within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the public contract for Platform health monitoring.</li>
 *   <li>Specifies WHAT the Health Service can do — implementations define HOW.</li>
 *   <li>Enables health checking across all Platform components.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.health.api
 * ├── HealthService.java       — Public health service contract
 * ├── HealthComponent.java     — Forward-reference placeholder (EIO-602)
 * ├── HealthStatus.java        — Forward-reference placeholder (EIO-602)
 * ├── HealthReport.java        — Forward-reference placeholder (EIO-602)
 * ├── HealthCheck.java         — Forward-reference placeholder (EIO-602)
 * ├── package-info.java
 * └── README.md
 * </pre>
 *
 * <p><b>Expected Future Package Structure:</b></p>
 * <pre>
 * platform.core.health
 * ├── api        — Public contracts (this package)
 * ├── model      — Domain models (EIO-602)
 * ├── validator  — Validation logic
 * ├── error      — Error types
 * ├── engine     — Health check execution
 * ├── service    — Implementation
 * └── tests      — Verification suite
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Out of Scope:</b></p>
 * <ul>
 *   <li>Models — {@code HealthComponent}, {@code HealthStatus}, {@code HealthReport}, {@code HealthCheck} are forward references for EIO-602.</li>
 *   <li>Implementation — no implementation classes in this package.</li>
 *   <li>Validation — validation logic belongs in the implementation.</li>
 *   <li>Storage — health state storage belongs in the implementation.</li>
 *   <li>Monitoring — monitoring logic belongs in the implementation.</li>
 *   <li>Scheduling — health check scheduling belongs in the implementation.</li>
 *   <li>Threading — threading model belongs in the implementation.</li>
 *   <li>Persistence — persistence belongs in the implementation.</li>
 *   <li>Caching — caching belongs in the implementation.</li>
 *   <li>Events — event publishing belongs in the implementation.</li>
 * </ul>
 *
 * @see com.shreeai.os.platform.core.health.api.HealthService
 */
package com.shreeai.os.platform.core.health.api;