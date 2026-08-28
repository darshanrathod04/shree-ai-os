/**
 * <b>Discovery Error Architecture</b>
 *
 * <p>Standardized error model for the Discovery Service within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a standardized error model for the Discovery Service.</li>
 *   <li>Defines error codes, structured error descriptions, and a base exception hierarchy.</li>
 *   <li>Mirrors the approved Registry Error Architecture for consistency across Platform Core Services.</li>
 *   <li>Ensures all discovery errors are consistent, typed, and documented.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.discovery.error
 * ├── DiscoveryErrorCode.java              — Standardized error codes
 * ├── DiscoveryError.java                  — Immutable error description
 * ├── DiscoveryException.java              — Base exception
 * ├── CapabilityNotFoundException.java
 * ├── ContractNotFoundException.java
 * └── InvalidDiscoveryRequestException.java
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> CONST-001, KERNEL-006, ADD-PLT-202,
 * ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>All error types are immutable where applicable.</li>
 *   <li>DiscoveryException is the ONLY base exception — all future exceptions extend it.</li>
 *   <li>No business logic — error definitions only.</li>
 *   <li>No Spring annotations — framework-agnostic.</li>
 * </ul>
 *
 * @see com.shreeai.os.platform.core.discovery.error.DiscoveryErrorCode
 * @see com.shreeai.os.platform.core.discovery.error.DiscoveryError
 * @see com.shreeai.os.platform.core.discovery.error.DiscoveryException
 */
package com.shreeai.os.platform.core.discovery.error;