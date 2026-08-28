/**
 * <b>Discovery Service Implementation</b>
 *
 * <p>Default implementation of the Discovery Service within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the reference implementation of capability resolution.</li>
 *   <li>Resolves capabilities by consulting the Kernel Registry — it does not maintain its own registry.</li>
 *   <li>Ensures the Registry remains the single source of truth for kernel information.</li>
 *   <li>Never duplicates Registry state or creates separate storage structures.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.discovery.service
 * └── DefaultDiscoveryService.java  — Reference implementation
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> CONST-001, KERNEL-006, ADD-PLT-202,
 * ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Constructor injection only — no setter injection.</li>
 *   <li>Thread-safe — uses concurrent-safe Registry operations.</li>
 *   <li>Stateless — no internal state beyond injected dependencies.</li>
 *   <li>Never bypasses validator — all requests validated before resolution.</li>
 *   <li>Never bypasses error architecture — uses DiscoveryException hierarchy.</li>
 *   <li>Never duplicates Registry data — Registry is the single source of truth.</li>
 *   <li>Never exposes mutable collections — returns unmodifiable views.</li>
 *   <li>No internal storage, no cache, no persistence.</li>
 * </ul>
 *
 * @see com.shreeai.os.platform.core.discovery.service.DefaultDiscoveryService
 * @see com.shreeai.os.platform.core.discovery.api.DiscoveryService
 * @see com.shreeai.os.platform.core.registry.api.KernelRegistry
 */
package com.shreeai.os.platform.core.discovery.service;