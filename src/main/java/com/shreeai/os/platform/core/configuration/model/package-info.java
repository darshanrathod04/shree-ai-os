/**
 * <b>Configuration Domain Models</b>
 *
 * <p>Defines the immutable Platform Language for the Configuration Service within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the domain models for configuration management.</li>
 *   <li>Provides immutable value objects for type-safe configuration representation.</li>
 *   <li>Enables consistent configuration handling across Platform components.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.configuration.model
 * ├── ConfigurationKey.java         — Immutable configuration key
 * ├── ConfigurationNamespace.java   — Immutable configuration namespace
 * ├── ConfigurationType.java        — Enum of configuration types
 * ├── ConfigurationEntry.java       — Immutable configuration entry
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
 * @see com.shreeai.os.platform.core.configuration.model.ConfigurationKey
 * @see com.shreeai.os.platform.core.configuration.model.ConfigurationNamespace
 * @see com.shreeai.os.platform.core.configuration.model.ConfigurationType
 * @see com.shreeai.os.platform.core.configuration.model.ConfigurationEntry
 */
package com.shreeai.os.platform.core.configuration.model;