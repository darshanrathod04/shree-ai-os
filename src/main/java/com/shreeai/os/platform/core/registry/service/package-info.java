/**
 * <b>Kernel Registry Service</b>
 *
 * <p>Service implementations for the Kernel Registry within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the default in-memory implementation of the {@link com.shreeai.os.platform.core.registry.api.KernelRegistry}.</li>
 *   <li>Serves as the official reference implementation for the Platform Foundation.</li>
 *   <li>Owns the registry storage and enforces all registration, unregistration, and lookup operations.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.registry.service
 * └── DefaultKernelRegistry.java  — Default in-memory registry implementation
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> CONST-001, KERNEL-005, KERNEL-007,
 * ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Thread-safe — uses concurrent data structures.</li>
 *   <li>Constructor injection only — no setter injection.</li>
 *   <li>Never bypasses validation — all registrations are validated.</li>
 *   <li>Never bypasses error architecture — uses RegistryException hierarchy.</li>
 *   <li>Never modifies RegisteredKernel — models are immutable.</li>
 *   <li>Never exposes internal collections — returns unmodifiable views.</li>
 *   <li>In-memory only — no persistence, no caching, no serialization.</li>
 * </ul>
 *
 * @see com.shreeai.os.platform.core.registry.service.DefaultKernelRegistry
 */
package com.shreeai.os.platform.core.registry.service;