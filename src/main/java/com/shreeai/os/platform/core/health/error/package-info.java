/**
 * <b>Health Error Architecture</b>
 *
 * <p>Provides the error handling infrastructure for the Health subsystem within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines error codes for all Health subsystem error conditions.</li>
 *   <li>Provides immutable error model for structured error reporting.</li>
 *   <li>Provides base exception and concrete exception types.</li>
 *   <li>Enables consistent error handling across the Health subsystem.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.health.error
 * ├── HealthErrorCode.java              — Error code enum
 * ├── HealthError.java                  — Immutable error model
 * ├── HealthException.java              — Base runtime exception
 * ├── HealthComponentNotFoundException.java — Component not found
 * ├── HealthCheckFailedException.java   — Check failed
 * ├── InvalidHealthComponentException.java — Invalid component
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
 *   <li>Immutable — all error models are immutable value objects.</li>
 *   <li>No monitoring — errors only, no health evaluation.</li>
 *   <li>No service logic — error definitions only.</li>
 *   <li>No validation logic — validation belongs to the validator layer.</li>
 *   <li>No Spring — framework-agnostic.</li>
 *   <li>No persistence — no ORM annotations.</li>
 *   <li>No threads — no threading or scheduling.</li>
 *   <li>No event publishing — errors are thrown, not published.</li>
 * </ul>
 *
 * <p><b>Error Principle:</b> Every Platform Core Service owns its own Error Architecture.
 * All Error Architectures follow one Platform pattern.</p>
 *
 * @see com.shreeai.os.platform.core.health.error.HealthErrorCode
 * @see com.shreeai.os.platform.core.health.error.HealthError
 * @see com.shreeai.os.platform.core.health.error.HealthException
 * @see com.shreeai.os.platform.core.health.error.HealthComponentNotFoundException
 * @see com.shreeai.os.platform.core.health.error.HealthCheckFailedException
 * @see com.shreeai.os.platform.core.health.error.InvalidHealthComponentException
 */
package com.shreeai.os.platform.core.health.error;