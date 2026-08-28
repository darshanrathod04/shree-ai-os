/**
 * <b>Discovery Service Public API</b>
 *
 * <p>The Discovery Service enables capability resolution within Shree AI OS
 * without creating compile-time dependencies between kernels.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the official Platform contract for capability resolution.</li>
 *   <li>Enables kernels to discover other kernels by capability or contract.</li>
 *   <li>Hides deployment details from requesting kernels.</li>
 *   <li>Ensures discovery remains independent of registry implementation.</li>
 * </ul>
 *
 * <p><b>Package Structure:</b></p>
 * <pre>
 * platform.core.discovery.api
 * └── DiscoveryService.java  — Public discovery contract
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Constitutional Authority:</b> CONST-001, KERNEL-006, ADD-PLT-202,
 * ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Out of Scope:</b></p>
 * <ul>
 *   <li>Models — capability and contract types are defined by the implementation.</li>
 *   <li>Validation — validation logic belongs in the implementation layer.</li>
 *   <li>Errors — exception types are defined by the implementation.</li>
 *   <li>Service — implementation classes are not part of this package.</li>
 *   <li>Tests — testing is handled by the implementation.</li>
 * </ul>
 *
 * @see com.shreeai.os.platform.core.discovery.api.DiscoveryService
 */
package com.shreeai.os.platform.core.discovery.api;